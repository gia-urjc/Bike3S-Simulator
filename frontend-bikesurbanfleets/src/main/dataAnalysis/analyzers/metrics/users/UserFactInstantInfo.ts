import { TimeEntry, Event } from '../../../systemDataTypes/SystemInternalData';
import { Observer } from '../../ObserverPattern';

export class UserInstant {
    exitReason: string;
    appearanceInstant: number;
    rentalInstant: number;
    returnInstant: number; 
    abandonmentInstant: number;
    
    public constructor(reason: string, appearance: number, rental: number, returnTime: number, abandonment: number) {
        this.exitReason = reason;
        this.appearanceInstant = appearance;
        this.rentalInstant = rental;
        this.returnInstant = returnTime;
        this.abandonmentInstant = abandonment;
    }
}

export class UserFactInstantInfo implements Observer {
    private instantsPerUser: Map<number, UserInstant>;
    
    public constructor() {
        this.instantsPerUser = new Map();
    }
    
    private getUserInfo(involvedentities:any): UserInstant{
        for(let ent of involvedentities) {
            if(ent.type==='users'){
                const id= ent.id;
                if(!this.instantsPerUser.has(id)) {
                    this.instantsPerUser.set(id, new UserInstant('', 0, 0, 0, 0));
                }
                return this.instantsPerUser.get(id);
            }
        }
        return undefined;
    }
 
    
    public update(timeEntry: TimeEntry): void {
        
        for(let event of timeEntry.events) {
            const info: UserInstant |undefined =this.getUserInfo(event.involvedEntities);
            switch(event.name) {
                case 'EventUserAppears': {
                    info.appearanceInstant = timeEntry.time;
                    break;
                }
                case 'EventUserArrivesAtStationToRentBike': {
                    if(event.result==='SUCCESSFUL_BIKE_RENTAL'){
                        info.rentalInstant = timeEntry.time;
                    }
                    break;
                }
                case 'EventUserArrivesAtStationToReturnBike': {
                    if(event.result==='SUCCESSFUL_BIKE_RETURN'){
                        info.returnInstant = timeEntry.time;
                    }
                    break;
                }
                case 'EventUserLeavesSystem': {
                    info.abandonmentInstant = timeEntry.time;
                    info.exitReason = event.result;
                    break;
                }
            }  
        }
    }
    
    public getInstantsPerUser(): Map<number, UserInstant> {
        return this.instantsPerUser; 
    }
  
}
 