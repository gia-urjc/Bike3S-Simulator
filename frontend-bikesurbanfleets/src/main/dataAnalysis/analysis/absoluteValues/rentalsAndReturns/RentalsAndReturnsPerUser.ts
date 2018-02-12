import { HistoryReader } from '../../../../util';
import { HistoryEntitiesJson } from '../../../../../shared/history';
import { HistoryIterator } from "../../../HistoryIterator";
import { Observer } from '../../ObserverPattern';
import  { User } from '../../../systemDataTypes/Entities';
import  { TimeEntry, Event } from '../../../systemDataTypes/SystemInternalData';
import { AbsoluteValue } from '../AbsoluteValue';
import { Data } from '../Data';

export class RentalsAndReturnsPerUser implements Data {
    private users: Array<User>;
    private bikeFailedRentalsPerUser: Map<number, AbsoluteValue>;
    private bikeSuccessfulRentalsPerUser: Map<number, AbsoluteValue>;
    private bikeFailedReturnsPerUser: Map<number, AbsoluteValue>;
    private bikeSuccessfulReturnsPerUser: Map<number, AbsoluteValue>;
  private factType: string;
  private entityType: string;
    
    public constructor() {
        this.factType = "RENTAL_AND_RETURN";
        this.entityType = "USER";
        this.bikeFailedRentalsPerUser = new Map<number, AbsoluteValue>();
        this.bikeSuccessfulRentalsPerUser = new Map<number, AbsoluteValue>();
        this.bikeFailedReturnsPerUser = new Map<number, AbsoluteValue>();
        this.bikeSuccessfulReturnsPerUser = new Map<number, AbsoluteValue>();
    }
  
    public getFactType(): string {
      return this.factType;
    }
  
    public getEntityType(): string {
      return this.entityType;
    }
    
    public async init(path: string): Promise<void> {
        try {
            let history: HistoryReader = await HistoryReader.create(path);
            let entities: HistoryEntitiesJson = await history.getEntities("users");
            this.users = entities.instances;
                
            for(let user of this.users) {
                this.bikeFailedRentalsPerUser.set(user.id, { name: "Failed bike rentals", value: 0 });
                this.bikeSuccessfulRentalsPerUser.set(user.id, { name: "Successful bike rentals", value: 0 });
                 this.bikeFailedReturnsPerUser.set(user.id, { name: "Failed bike returns", value: 0 });            
                this.bikeSuccessfulReturnsPerUser.set(user.id, { name: "Successful bike returns", value: 0 });            
            }
        }
        catch(error) {
            console.log('error getting users:', error);
        }
        return;
    }

    public static async create(path: string): Promise<RentalsAndReturnsPerUser> {
        let rentalsAndReturnsValues = new RentalsAndReturnsPerUser();
        try {
            await rentalsAndReturnsValues.init(path);
        }
        catch(error) {
            console.log(error);
        }
        return rentalsAndReturnsValues;
    }

    public getBikeFailedRentalsOfUser(userId: number): number | undefined {
        let absoluteValue: AbsoluteValue = this.bikeFailedRentalsPerUser.get(userId);
        return absoluteValue.value;
    }
    
    public getBikeSuccessfulRentalsOfUser(userId: number): number | undefined {
        let absoluteValue: AbsoluteValue = this.bikeSuccessfulRentalsPerUser.get(userId);
        return absoluteValue.value;
    }
    
    public getBikeFailedReturnsOfUser(userId: number): number | undefined {
        let absoluteValue: AbsoluteValue = this.bikeFailedReturnsPerUser.get(userId);
        return absoluteValue.value;
    }
    
    public getBikeSuccessfulReturnsOfUser(userId: number): number | undefined {
        let absoluteValue: AbsoluteValue = this.bikeSuccessfulReturnsPerUser.get(userId);
        return absoluteValue.value;
    }
  
  private increaseValue(data: Map<number, AbsoluteValue>, key: number): void { 
      let absoluteValue: AbsoluteValue = data.get(key);
      if (absoluteValue !== undefined) {  // a gotten map value could be undefined
          absoluteValue.value++;
      }
 }
  
    public update(timeEntry: TimeEntry): void {
        let events: Array<Event> = timeEntry.events;

        let key: number;

        for(let event of events) {
            if (event.name === 'EventUserArrivesAtStationToRentBikeWithReservation'
                && event.changes.users.length > 0) {
                key = event.changes.users[0].id;
                this.increaseValue(this.bikeSuccessfulRentalsPerUser, key);
            }
                
            else if (event.name === 'EventUserArrivesAtStationToReturnBikeWithReservation'
                &&  event.changes.users.length > 0) {
                key = event.changes.users[0].id;
                this.increaseValue(this.bikeSuccessfulReturnsPerUser, key);
            }
            
            else if (event.name === 'EventUserArrivesAtStationToRentBikeWithoutReservation'
                && event.changes.users.length > 0) {
                key = event.changes.users[0].id;
                let bike: any = event.changes.users[0].bike;
                if (bike !== undefined) {
                    this.increaseValue(this.bikeSuccessfulRentalsPerUser, key);
                }
                else {
                  this.increaseValue(this.bikeFailedRentalsPerUser, key);
                }
            }
    
            else if (event.name === 'EventUserArrivesAtStationToReturnBikeWithoutReservation'
                && event.changes.users.length > 0) {
                key = event.changes.users[0].id;
                let bike: any = event.changes.users[0].bike;
                if (bike !== undefined) {
                    this.increaseValue(this.bikeSuccessfulRentalsPerUser, key);
                }
                else {
                  this.increaseValue(this.bikeFailedRentalsPerUser, key);
                }
            }
        }
    }
  
  public toString(): string {
    let str: string = '';
    this.bikeFailedReturnsPerUser.forEach( (absoluteValue, key) => 
        str += 'User: '+key+' '+absoluteValue.name+': '+absoluteValue.value+'\n');
    this.bikeFailedRentalsPerUser.forEach( (absoluteValue, key) => 
        str += 'User: '+key+' '+absoluteValue.name+': '+absoluteValue.value+'\n');
    this.bikeSuccessfulReturnsPerUser.forEach( (absoluteValue, key) => 
        str += 'User: '+key+' '+absoluteValue.name+': '+absoluteValue.value+'\n');
    this.bikeSuccessfulRentalsPerUser.forEach( (absoluteValue, key) => 
        str += 'User: '+key+' '+absoluteValue.name+': '+absoluteValue.value+'\n');
      
      return str;
  }
  
       
}