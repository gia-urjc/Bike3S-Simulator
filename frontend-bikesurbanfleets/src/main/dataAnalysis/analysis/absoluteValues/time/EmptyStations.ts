import { Info } from "../Info";
import { BikesPerStation, StationBikesPerTimeList } from './BikesPerStation';

export interface TimeInterval {
    start: number;
    end: number;
}
 
export interface EmptyState {
    timeIntervals: Array<TimeInterval>;  
    totalTime: number;
}

export class EmptyStations implements Info {
    private emptyStatesPerStation: Map<number, EmptyState>;
   
    public constructor() {
        this.emptyStatesPerStation = new Map(); 
    }
       
    public init(stationsInfo: BikesPerStation): void {
        stationsInfo.getStations().forEach( (stationInfo, stationId) => {
            let emptyState: EmptyState = this.createEmptyStateFor(stationInfo);
            this.emptyStatesPerStation.set(stationId, emptyState);
        });
    }
    
    private createEmptyStateFor(stationInfo: StationBikesPerTimeList): EmptyState {
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
                    let interval: TimeInterval = {start: startTime, end: endTime};
                    intervals.push(interval);
                    time += interval.end - interval.start;
                    startTime = -1;
                    endTime = -1;
                }
            }
        });
        return {timeIntervals: intervals, totalTime: time};
    }
    
    public static create(stationsInfo: BikesPerStation): EmptyStations {
        let emptyStations: EmptyStations = new EmptyStations();
        emptyStations.init(stationsInfo);
        return emptyStations;
    }
    
    public getEmptyStatesPerStation(): Map<number, EmptyState> {  
        return this.emptyStatesPerStation;
    }

}
