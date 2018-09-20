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
    stations: Map<number, Station>;
    
    public constructor(bikesInfo: BikesPerStation) {
        this.basicData = bikesInfo;
        this.data = new BikesBalanceData();
        this.stations = new Map(); 
    }
    
    public setStations(stations: Array<Station>): void {
        for (let station of stations) {
        this.stations.set(station.id, station);
        }
    }
    
    private quality(capacity: number, list: Array<BikesPerTime>): number {
        let summation: number = 0;
        let individualValue: number = 0;
        let pastTime: number = 0;
        for (let i = 0; i < list.length; i++) {
            let stationBikes: BikesPerTime = list[i];
            individualValue = Math.pow(stationBikes.availableBikes - capacity/2, 2) * (stationBikes.time - pastTime);
            pastTime = stationBikes.time;
            summation += individualValue;
        }
        return summation;
    }
    
    public async init(): Promise<void> {
        this.basicData.getStations().forEach( (stationInfo, stationId) => {
            let station: Station = this.stations.get(stationId);
            let capacity: number = station.capacity; 
            let qualityValue: number = this.quality(capacity, stationInfo.getList());
            this.data.absoluteValues.set(stationId, new BikesBalanceAbsoluteValue(qualityValue));
        });
        return;
    }
    
    public getData(): Data {
        return this.data;
    }
}