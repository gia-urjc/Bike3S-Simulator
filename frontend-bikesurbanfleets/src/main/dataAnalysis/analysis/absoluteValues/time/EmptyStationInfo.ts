import { AbsoluteValue } from "../AbsoluteValue";
import { Data } from "../Data";
import { SystemInfo } from "../SystemInfo";
import { BikesPerStation, StationBikesPerTimeList } from './BikesPerStation';

export class TimeInterval {
    start: number;
    end: number;
    
    public constructor(start: number, end: number) {
        this.start = start;
        this.end = end;
    }
    
    public toString(): string {
        return "("+this.start+", "+this.end+")";
    }
}
 
export class EmptyStateAbsoluteValue implements AbsoluteValue {
    timeIntervals: Array<TimeInterval>;  
    totalTime: number;
    
    public constructor(intervals: Array<TimeInterval>, time: number) {
        this.timeIntervals = intervals;
        this.totalTime = time;
    }
    
    public intervalsToString(): string {
        let str: string = "";
        this.timeIntervals.forEach( (interval) => {
           str += interval.toString()+" "; 
        });
        return str;
    }
}

export class EmptyStateData implements Data {
    static readonly NAMES: Array<string> = ['Time intervals', 'Total time'];
    absoluteValues: Map<number, AbsoluteValue>;
    
    constructor() {
       this.absoluteValues = new Map<number, AbsoluteValue>();
    }
}

export class EmptyStationInfo implements SystemInfo {
    basicData: BikesPerStation;
    data: Data;
   
    public constructor(stationsInfo: BikesPerStation) {
        this.basicData = stationsInfo;
        this.data = new EmptyStateData(); 
    }
       
     public async init(): Promise<void> {
        this.basicData.getStations().forEach( (stationInfo, stationId) => {
            let emptyState: EmptyStateAbsoluteValue = this.createEmptyStateFor(stationInfo);
            this.data.absoluteValues.set(stationId, emptyState);
        });
        return;
    }
    
    private createEmptyStateFor(stationInfo: StationBikesPerTimeList): EmptyStateAbsoluteValue {
        let intervals: Array<TimeInterval> = new Array();
        let time: number = 0;
        let startTime: number = -1;
        let endTime: number = -1;
         
        stationInfo.getList().forEach ( (bikesPerTime) => {
            if (startTime === -1) {
                if (bikesPerTime.availableBikes === 0) {
                    startTime = bikesPerTime.time;
                }
            }
            else {
                // TODO: add 1 instant to time if it is the last stationInfo data
                if (bikesPerTime.availableBikes !== 0) {
                    endTime = bikesPerTime.time;
                    let interval: TimeInterval = new TimeInterval(startTime, endTime);
                    intervals.push(interval);
                    time += interval.end - interval.start;
                    startTime = -1;
                    endTime = -1;
                }
            }
        });
        return new EmptyStateAbsoluteValue(intervals, time);
    }
    
    public static create(stationsInfo: BikesPerStation): EmptyStationInfo {
        let emptyStations: EmptyStationInfo = new EmptyStationInfo(stationsInfo);
        emptyStations.init();
        return emptyStations;
    }
    
    public getData(): Data {  
        return this.data;
    }

}
