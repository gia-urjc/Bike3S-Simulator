import { Station } from '../../../systemDataTypes/Entities';
import { AbsoluteValue } from '../AbsoluteValue';
import { Data } from '../Data';
import { SystemInfo } from '../SystemInfo';
import { BikesPerTime, BikesPerStation, StationBikesPerTimeList } from './BikesPerStation';

export class BikesBalanceAbsoluteValue implements AbsoluteValue {
    quality: number;
    
    constructor(quality: number) {
        this.quality = quality;
    }
}

export class BikesBalanceData implements Data {
    static readonly NAMES: Array<string> = ['Bikes balance of station'];
    absoluteValues: Map<number, AbsoluteValue>;
        
    public constructor() {
       this.absoluteValues = new Map<number, AbsoluteValue>();
    }
}

export class BikesBalanceQuality implements SystemInfo {
    basicData: BikesPerStation;
    data: Data;
    stations: Array<Station>;
    
    public constructor(bikesInfo: BikesPerStation) {
        this.basicData = bikesInfo;
        this.data = new BikesBalanceData(); 
    }
    
    public setStations(stations: Array<Station>): void {
        this.stations = stations;
    }
    
    private quality(capacity: number, list: Array<BikesPerTime>): number {
        let summation: number = 0;
        let individualValue: number = 0;
        let pastTime: number = 0;
        for (let i = 0; i < list.length; i++) {
            let station: BikesPerTime = list[i];
            individualValue = Math.abs( Math.pow(station.availableBikes - capacity/2, 2) * (station.time - pastTime));
            pastTime = station.time;
            summation += individualValue;
        }
        return summation;
    }
    
    public async init(): Promise<void> {
        let i: number = 0;   // stations counter
        this.basicData.getStations().forEach( (stationInfo, stationId) => {
            let station: Station = this.stations[i];
            let capacity: number = station.capacity; 
            let qualityValue: number = this.quality(capacity, stationInfo.getList());
            this.data.absoluteValues.set(stationId, new BikesBalanceAbsoluteValue(qualityValue));
            i++;
        })
        return;
    }
    
    public getData(): Data {
        return this.data;
    }
}