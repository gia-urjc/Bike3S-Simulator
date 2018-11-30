import  { TimeEntry, Event } from '../../../systemDataTypes/SystemInternalData';
import { Observer } from "../../ObserverPattern";
import { SystemInfo } from "../../SystemInfo";
import { RentalAndReturnData } from './RentalAndReturnData';

export class RentalsAndReturnsPerUser implements SystemInfo, Observer {
    data: RentalAndReturnData;

    public static async create() {
        return new RentalsAndReturnsPerUser();
    }
    
    public constructor() {
        this.data = new RentalAndReturnData();
    }
     public init(): void {}

    private getUserId(involvedentities:any): number|undefined {
        for(let ent of involvedentities) {
            if(ent.type==='users'){
                return ent.id;
            }
        }
        return undefined;
    }
    
    public update(timeEntry: TimeEntry): void {
        for(let event of timeEntry.events) {
            
            switch(event.name) {
                case 'EventUserArrivesAtStationToReturnBike': {
                    const key=this.getUserId(event.involvedEntities);
                    if (event.result==='SUCCESSFUL_BIKE_RETURN') { 
                        this.data.increaseSuccessfulReturns(key);
                    } else if (event.result==='FAILED_BIKE_RETURN') { 
                        this.data.increaseFailedReturns(key);
                    }
                    break;
                }
                 case 'EventUserArrivesAtStationToRentBike': {
                    const key=this.getUserId(event.involvedEntities);
                    if (event.result==='SUCCESSFUL_BIKE_RENTAL') { 
                        this.data.increaseSuccessfulRentals(key);
                    } else if (event.result==='FAILED_BIKE_RENTAL') { 
                        this.data.increaseFailedRentals(key);
                    }
                    break;
                }
            }
        }
    }

    public getData(): RentalAndReturnData {
        return this.data;
    }
      
}