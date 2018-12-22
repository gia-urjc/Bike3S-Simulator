import { TimeEntry, Event } from "../../../systemDataTypes/SystemInternalData";
import { Observer } from "../../ObserverPattern";
import HistoryEntityStation from "../../../historyEntities/HistoryEntityStation";
import HistoryStationChanges from "../../../historyEntities/HistoryStationChanges";

export interface BikesPerTime {
    time: number;
    availableBikes: number;
}

export class StationBikesPerTimeList {
    private bikesPerTimeList: Array<BikesPerTime>;
    
    public constructor(bikes: number) {
        this.bikesPerTimeList = new Array();
        let value: BikesPerTime = {time: 0, availableBikes: bikes};
        this.bikesPerTimeList.push(value);
    }
  
    public addNewEntry(time: number, value: number): void {
        this.bikesPerTimeList.push({time: time, availableBikes: value});
    }  

    public getList(): Array<BikesPerTime> {
        return this.bikesPerTimeList;
    }
}

export class BikesPerStationAndTime implements Observer {
    private stations: Map<number, StationBikesPerTimeList>;

    public constructor() {
        this.stations = new Map();
    }
      
    public async init(systemStations: Array<HistoryEntityStation>) {
        for(let station of systemStations) {
            let value: StationBikesPerTimeList = new StationBikesPerTimeList(station.availablebikes);
            this.stations.set(station.id, value);
        }
    }
  
    public update(timeEntry: TimeEntry): void {
        let instant: number = timeEntry.time;
        let events: Array<Event> = timeEntry.events;
        
        for(let event of events) {
            if (event.changes && event.changes.stations) {
                let stationchanges: Array<HistoryStationChanges> = event.changes.stations;
                for (let stationchange of stationchanges) {
                    if(stationchange.availablebikes) {
                        let stationBikesPerTime: StationBikesPerTimeList | undefined = this.stations.get(stationchange.id);
                        if(!stationBikesPerTime) {
                            throw new Error("Station Bikes per time List not found for id:" + stationchange.id);
                        }
                        stationBikesPerTime.addNewEntry(instant,stationchange.availablebikes.new);
                    }
                } 
            }
        }
    }
    

    
    public getStations(): Map<number, StationBikesPerTimeList> {
        return this.stations;
    } 


}
