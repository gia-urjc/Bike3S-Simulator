import  { TimeEntry, Event } from '../../../systemDataTypes/SystemInternalData';
import { Observer } from "../../ObserverPattern";
import { Data } from "../../Data";
import { SystemInfo } from "../../SystemInfo";
import { RentalAndReturnData } from './RentalAndReturnData';

export class RentalsAndReturnsPerStation implements SystemInfo, Observer {
    data: Data;

    public static async create() {
        return new RentalsAndReturnsPerStation();
    }

    public constructor() {
        this.data = new RentalAndReturnData();
    }
    public init(): void {}

   private getStationId(involvedentities:any): number|undefined{
        for(let ent of involvedentities) {
            if(ent.type==='stations'){
                return ent.id;
            }
        }
        return undefined;
    }
    
    public update(timeEntry: TimeEntry): void {
        for(let event of timeEntry.events) {
             switch(event.name) {
                case 'EventUserArrivesAtStationToReturnBike': {
                    const key=this.getStationId(event.involvedEntities);
                    if(!key) {
                        throw new Error("Station can't be found. Involved entity: \n" + event.involvedEntities);
                    }
                    if (event.result==='SUCCESSFUL_BIKE_RETURN') { 
                        this.data.increaseSuccessfulReturns(key);
                    } else if (event.result==='FAILED_BIKE_RETURN') { 
                        this.data.increaseFailedReturns(key);
                    }
                    break;
                }
                 case 'EventUserArrivesAtStationToRentBike': {
                    const key=this.getStationId(event.involvedEntities);
                    if(!key) {
                        throw new Error("Station can't be found. Involved entity: \n" + event.involvedEntities);
                    }
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
    
    public getData(): Data {
        return this.data;
    }

}
