import { Station } from '../../../systemDataTypes/Entities';
import { AbsoluteValue } from '../AbsoluteValue';
import { Data } from '../Data';
import { SystemInfo } from '../SystemInfo';
import { BikesPerTime, BikesPerStationAndTime, StationBikesPerTimeList } from './BikesPerStationAndTime';

export class StationBalancingAbsoluteValue implements AbsoluteValue {
    quality: number;
    
    constructor(quality: number) {
        this.quality = quality;
    }
}

export class StationBalancingData implements Data {
    static readonly NAMES: string = 'balancing quality';
    absoluteValues: Map<number, AbsoluteValue>;
        
    public constructor() {
       this.absoluteValues = new Map<number, AbsoluteValue>();
    }
}

export class StationBalancingQuality implements SystemInfo {
    basicData: BikesPerStationAndTime;
    data: Data;
    stations: Map<number, Station>;
    
    public constructor(bikesInfo: BikesPerStationAndTime) {
        this.basicData = bikesInfo;
        this.data = new StationBalancingData();
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
        let pastTime: number = 0;  // in hours
        for (let i = 0; i < list.length; i++) {
            let stationBikes: BikesPerTime = list[i];
            individualValue = Math.abs(stationBikes.availableBikes - capacity/2) * (stationBikes.time/3600 - pastTime);
            pastTime = stationBikes.time/3600;
            summation += individualValue;
        }
        return summation;
    }
    
    public async init(): Promise<void> {
        this.basicData.getStations().forEach( (stationInfo, stationId) => {
            let station: Station | undefined = this.stations.get(stationId);
            if (station) {
                let capacity: number = station.capacity;
                let qualityValue: number = this.quality(capacity, stationInfo.getList());
                this.data.absoluteValues.set(stationId, new StationBalancingAbsoluteValue(qualityValue));
                let abs: AbsoluteValue | undefined = this.data.absoluteValues.get(stationId); 
            }
        });
        return;
    }
    
    public getData(): Data {
        return this.data;
    }
}