import { User } from '../../../systemDataTypes/Entities';
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
    
    public async init(users: Array<User>): Promise<void> {
        for (let user of users) {
            this.instantsPerUser.set(user.id, new UserInstant('', 0, 0, 0, 0));
        }
        return;
    }
    
    public update(timeEntry: TimeEntry): void {
        let events: Array<Event> = timeEntry.events;
        let user: User;
        let key: number;
        let info: UserInstant | undefined;
        
        for(let event of events) {
            user = event.changes.users[0];
            key = user.id;
            info = this.instantsPerUser.get(key);
            
            switch(event.name) {
                case 'EventUserAppears': {
                    if (info) { 
                        info.appearanceInstant = timeEntry.time;
                    }
                    break;
                }
                    
                case 'EventUserArrivesAtStationToRentBikeWithReservation': {
                    if (info) {
                        info.rentalInstant = timeEntry.time;
                    }
                    break;
                }
                    
                case 'EventUserArrivesAtStationToRentBikeWithoutReservation': {
                    if (info && user.bike) {
                        info.rentalInstant = timeEntry.time;
                    }
                    break;
                }
                case 'EventUserArrivesAtStationToReturnBikeWithReservation': {   
                    if (info) {
                        info.returnInstant = timeEntry.time;
                    }
                    break;
                }
                    
                case 'EventUserArrivesAtStationToReturnBikeWithoutReservation': {    
                    if (info && user.bike) {
                        info.returnInstant = timeEntry.time;
                    }
                    break;
                }
                    
                case 'EventUserLeavesSystem': {
                    if (info) {
                        info.abandonmentInstant = timeEntry.time;
                    }
                    info.exitReason = user.state;
                    break;
                }
            }  
        }
    }
    
    public getInstantsPerUser(): Map<number, UserInstant> {
        return this.instantsPerUser; 
    }
  
}
 