import { User } from '../../../systemDataTypes/Entities';
import { TimeEntry, Event } from '../../../systemDataTypes/SystemInternalData';
import { AbsoluteValue } from '../AbsoluteValue';
import { Data } from '../Data';
import { SystemInfo } from '../SystemInfo';

export class UserTimeAbsoluteValue implements AbsoluteValue {
    timeappeared: number;
    timegetbike: number;
    timeleavebike: number;
    timegetdestination: number;
    timeleave: number;
    EXIT_AFTER_TIMEOUT: boolean;
    EXIT_AFTER_FAILED_RESERVATION: boolean;
    EXIT_AFTER_FAILED_RENTAL: boolean;
    EXIT_AFTER_REACHING_DESTINATION: boolean;
 
    public constructor() {
        this.timeappeared = 0;
        this.timegetbike = 0;
        this.timeleavebike = 0;
        this.timegetdestination = 0;
        this.timeleave = 0;
        this.EXIT_AFTER_TIMEOUT = false;
        this.EXIT_AFTER_FAILED_RESERVATION = false;
        this.EXIT_AFTER_FAILED_RENTAL = false;
        this.EXIT_AFTER_REACHING_DESTINATION = false;
    }
}

export class UserTimeData implements Data {
    static readonly NAMES: string = 'time at system';
    absoluteValues: Map<number, AbsoluteValue>;
     
     public constructor() {
         this.absoluteValues = new Map();
     }
    
}
  
export class UserTimeAtSystem implements SystemInfo  {
    basicData: Array<User>;
    data: Data;
    
    public constructor(users: Array<User>) {
        this.basicData = users;
        this.data = new UserTimeData();
    }
    
    public async init(): Promise<void> {
        for (let user of this.basicData) {
            this.data.absoluteValues.set(user.id, new UserTimeAbsoluteValue());
        }
        return;
    }
    
    public update(timeEntry: TimeEntry): void {
        let events: Array<Event> = timeEntry.events;
        let key: number;
        for(let event of events) {
            key = event.changes.users[0].id;
            
            switch(event.name) {
                case 'EventUserAppears': {
                    let value: AbsoluteValue | undefined = this.data.absoluteValues.get(key);
                    if (value) { 
                        value.timeappeared = timeEntry.time;
                    }
                    break;
                }
                case 'EventUserArrivesAtStationToRentBikeWithoutReservation': {
                    let value: AbsoluteValue | undefined = this.data.absoluteValues.get(key);
                    if (value && event.changes.users[0].state!== undefined && 
                            event.changes.users[0].state.old === 'WALK_TO_STATION' &&
                            event.changes.users[0].state.new === 'WITH_BIKE' ) { 
                                value.timegetbike = timeEntry.time;
                    }
                    break;
                }
                case 'EventUserArrivesAtStationToRentBikeWithReservation': {
                    let value: AbsoluteValue | undefined = this.data.absoluteValues.get(key);
                    if (value && event.changes.users[0].state!== undefined && 
                            event.changes.users[0].state.old === 'WALK_TO_STATION' &&
                            event.changes.users[0].state.new === 'WITH_BIKE' ) { 
                                value.timegetbike = timeEntry.time;
                    }
                    break;
                }
                case 'EventUserArrivesAtStationToReturnBikeWithoutReservation': {
                    let value: AbsoluteValue | undefined = this.data.absoluteValues.get(key);
                    if (value && event.changes.users[0].state!== undefined && 
                            event.changes.users[0].state.old === 'WITH_BIKE') { 
                                value.timeleavebike = timeEntry.time;
                    }
                    break;
                }
               case 'EventUserArrivesAtStationToReturnBikeWithReservation': {
                    let value: AbsoluteValue | undefined = this.data.absoluteValues.get(key);
                    if (value && event.changes.users[0].state!== undefined && 
                            event.changes.users[0].state.old === 'WITH_BIKE') { 
                                value.timeleavebike = timeEntry.time;
                    }
                    break;
                }
                case 'EventUserArrivesAtDestinationInCity': {
                    let value: AbsoluteValue | undefined = this.data.absoluteValues.get(key);
                    if (value && event.changes.users[0].state!== undefined && 
                            event.changes.users[0].state.old === 'WALK_TO_DESTINATION') { 
                                value.timegetdestination = timeEntry.time;
                    }
                    break;
                }
                case 'EventUserLeavesSystem': {
                    let value: AbsoluteValue | undefined = this.data.absoluteValues.get(key);
                    if (value && event.changes.users[0].state!== undefined ) {
                        value.timeleave = timeEntry.time;
                        switch(event.changes.users[0].state.old) {
                           case 'EXIT_AFTER_TIMEOUT': {
                                value.EXIT_AFTER_TIMEOUT = true;
                                break;
                           }
                            case 'EXIT_AFTER_FAILED_RESERVATION': {
                                value.EXIT_AFTER_FAILED_RESERVATION = true;
                                break;
                           }
                           case 'EXIT_AFTER_FAILED_RENTAL': {
                                value.EXIT_AFTER_FAILED_RENTAL = true;
                                break;
                           }
                           case 'EXIT_AFTER_REACHING_DESTINATION': {
                                value.EXIT_AFTER_REACHING_DESTINATION = true;
                                break;
                           }
                        }
                    }
                }
         
            }  //for
        }
    }
        
        public getData(): Data {
            return this.data;
        }
        
} 