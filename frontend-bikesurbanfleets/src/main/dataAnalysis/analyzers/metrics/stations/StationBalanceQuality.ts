import { AbsoluteValue } from '../../AbsoluteValue';
import { Data } from '../../Data';
import { SystemInfo } from '../../SystemInfo';
import { BikesPerTime, BikesPerStationAndTime } from './BikesPerStationAndTime';
import HistoryEntityStation from '../../../historyEntities/HistoryEntityStation';


export class StationBalanceAbsoluteValue implements AbsoluteValue {
    quality: number;
    
    constructor(quality: number) {
        this.quality = quality;
    }
}

export class StationBalanceData implements Data {
    static readonly NAMES: string = 'balancing quality';
    absoluteValues: Map<number, AbsoluteValue>;
        
    public constructor() {
       this.absoluteValues = new Map<number, AbsoluteValue>();
    }
}

export class StationBalanceQuality implements SystemInfo {
    basicData: BikesPerStationAndTime;
    data: Data;
    stations: Map<number, HistoryEntityStation>;
    totalSimulationTime: number;
    
    public constructor(bikesInfo: BikesPerStationAndTime, time: number) {
        this.basicData = bikesInfo;
        this.data = new StationBalanceData();
        this.stations = new Map();
        this.totalSimulationTime =  time;
    }
    
    public setStations(stations: Array<HistoryEntityStation>): void {
        for (let station of stations) {
        this.stations.set(station.id, station);
        }
    }
    
    private quality(capacity: number, list: Array<BikesPerTime>): number {
        let summation: number = 0;
        let pastTime: number = 0;  // in hours
        let stationBikes: BikesPerTime;
        
        for (let i = 0; i < list.length; i++) {
            stationBikes = list[i];
            summation += Math.abs(stationBikes.availableBikes - capacity/2) * (stationBikes.time - pastTime);
            pastTime = stationBikes.time;
        }
        // @ts-ignore
        summation += Math.abs(stationBikes.availableBikes - capacity/2) * (this.totalSimulationTime - pastTime);
        return summation/this.totalSimulationTime;
    }
    
    public init(): void {
        this.basicData.getStations().forEach( (stationInfo, stationId) => {
            let station: HistoryEntityStation | undefined = this.stations.get(stationId);
            if (station) {
                let capacity: number = station.capacity;
                let qualityValue: number = this.quality(capacity, stationInfo.getList());
                this.data.absoluteValues.set(stationId, new StationBalanceAbsoluteValue(qualityValue));
                let abs: AbsoluteValue | undefined = this.data.absoluteValues.get(stationId); 
            }
        });
        return;
    }
    
    public getData(): Data {
        return this.data;
    }
}