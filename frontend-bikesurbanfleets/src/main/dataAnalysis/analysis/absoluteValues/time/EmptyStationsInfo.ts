import { BikesPerStationInfo, StationBikesPerTimeList } from './BikesPerStationInfo';

interface TimeInterval {
    start: number;
    end: number;
}

interface EmptyState {
    timeIntervals: Array<TimeInterval>; 
    totalTime: number;
}

export class EmptyStationsInfo {
   private stationsEmptyStateInfo: Map<number, EmptyState>;
   
   private constructor() {
       this.stationsEmptyStateInfo = new Map(); 
   }
       
    private init(stationsInfo: BikesPerStationInfo): void {
        stationsInfo.forEach( (stationInfo, stationId)) => {
            let emptyState: EmptyState = this.getEmptyStateOf(stationInfo);
            this.stationsEmptyStateInfo.set(stationId, emptyState);
        }
    }
    
    private getEmptyStateOf(stationInfo: StationBikesPerTimeList) {
        let intervals: Array<TimeInterval> = new Array();
        let time: number = 0;
        let startTime, endTime: number = -1;
         
        stationInfo.forEach (bikesPerTime) => {
            if (startTime === -1) {
                if (bikesPerTime.availableBikes === 0) {
                    startTime = bikesPerTime.time;
                }
            }
            else {
                // TODO: add 1 instant to time if it is the last stationInfo data
                if (bikesPerTime.available !== 0) {
                    endTime = bikesPerTime.time;
                    let interval: TimeInterval = {start: startTime, end: endTime};
                    intervals.push(interval);
                    time += interval.end - interval.start;
                    starttTime = -1;
                    endTime = -1;
                }
            }
        }
        return {timeIntervals: intervals, totalTime: time};
    }
    
    public static create(stationsInfo: StationSinfo): EmptyStationsInfo {
        let emptyStations: EmptyStationsInfo = new EmptyStationsInfo();
        emptyStations.init(stationsInfo);
        return emptyStations;
    }

}
