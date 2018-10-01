import { User } from '../../../systemDataTypes/Entities';
import { TimeEntry, Event } from '../../../systemDataTypes/SystemInternalData';
import { AbsoluteValue } from '../AbsoluteValue';
import { Data } from '../Data';
import { SystemInfo } from '../SystemInfo';

export class UserTimeAbsoluteValue implements AbsoluteValue {
    time: number;
    
    public constructor(time: number) {
        this.time = time;
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
            this.data.absoluteValues.set(user.id, new UserTimeAbsoluteValue(0));
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
                        value.time = timeEntry.time;
                        console.log("appearance time of user "+key+": "+value.time);  
                    }
                    break;
                }
                    
            /*   case 'EventUserArrivesAtStationToReturnBikeWithReservation': {
                    let value: AbsoluteValue | undefined = this.data.absoluteValues.get(key);
                    if (value) {
                        let appearanceTime: number = value.time;
                        value.time = timeEntry.time - appearanceTime;          
                    }
                    break;
                }
                    
                case 'EventUserArrivesAtStationToReturnBikeWithoutReservation': {
                    console.log(event.changes.users[0]);
                    let bike: any = event.changes.users[0].bike;

                    if (!bike) {
                        let value: AbsoluteValue | undefined = this.data.absoluteValues.get(key);
                        if (value) {
                            let appearanceTime: number = value.time;
                            value.time = timeEntry.time - appearanceTime;
                        }
                    }
                    break;
                }
              */
                    
                case 'EventUserArrivesAtDestinationInCity': {
                    let value: AbsoluteValue | undefined = this.data.absoluteValues.get(key);
                    if (value) {
                        let appearanceTime: number = value.time;
                        value.time = timeEntry.time - appearance;
                    }
                }
                }
                    
                }
            }
        }
    }
        
        public getData(): Data {
            return this.data;
        }
        
} 