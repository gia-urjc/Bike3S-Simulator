import { User } from '../../../systemDataTypes/Entities';
import { TimeEntry, Event } from '../../../systemDataTypes/SystemInternalData';
import { AbsoluteValue } from '../../AbsoluteValue';
import { Data } from '../../Data';
import { Observer } from '../../ObserverPattern';
import { SystemInfo } from '../../SystemInfo';
import { UserFactInstantInfo } from './UserFactInstantInfo';

export class UserTimeAbsoluteValue implements AbsoluteValue {
    static readonly NUM_ATTR: number = 4;
    exitReason: string;
    timeFromApperancePlaceToOriginStation: number;
    timeFromOriginStationToDestinationStation: any;
    timeFromDestinationStationToAbandonmentPlace: any;
    
    public constructor(reason: string, origin: number, midle: number, destination: number) {
        this.exitReason = reason;
        this.timeFromApperancePlaceToOriginStation = origin;
        this.timeFromOriginStationToDestinationStation = midle;
        this.timeFromDestinationStationToAbandonmentPlace = destination;
    }
    
    public getAbsoluteValuesAsArray(): Array<any> {
        let array: Array<any> = new Array();
        array.push(this.timeFromApperancePlaceToOriginStation);
        array.push(this.timeFromOriginStationToDestinationStation);
        array.push(this.timeFromDestinationStationToAbandonmentPlace);
        array.push(this.exitReason);
        return array;
    }
}

export class UserTimeData implements Data {
    static readonly NAMES: Array<string> = ['time to origin station',
        'cycling time', 
        'time to destination place',
    'exit reason'];
    absoluteValues: Map<number, AbsoluteValue>;
     
     public constructor() {
         this.absoluteValues = new Map();
     }
    
}
  
export class UserTimeAtSystem implements SystemInfo {
    basicData: UserFactInstantInfo;
    data: Data;
    
    public constructor(users: UserFactInstantInfo) {
        this.basicData = users;
        this.data = new UserTimeData();
    }
    
    public async init(): Promise<void> {
        let firstTime: number;
        let secondTime: any;
        let thirdTime: any;
        
        this.basicData.getInstantsPerUser().forEach( (info, userId) => {
            if (info.rentalInstant !== 0) {
                firstTime = info.rentalInstant - info.appearanceInstant;
                secondTime = info.returnInstant - info.rentalInstant;
                thirdTime = info.abandonmentInstant - info.returnInstant;
            }
            else {   // user has left the system before renting a bike
                firstTime = info.abandonmentInstant - info.appearanceInstant;
                secondTime = "";
                thirdTime = "";
            }
            this.data.absoluteValues.set(userId, new UserTimeAbsoluteValue(info.exitReason, firstTime, secondTime, thirdTime));
        });
        return;
    }
    
    public getData(): Data {
        return this.data;
    }
        
} 