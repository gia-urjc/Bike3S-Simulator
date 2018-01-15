import { HistoryReader } from '../../../../util';
import { HistoryEntitiesJson } from '../../../../../shared/history';
import { HistoryIterator } from "../../../HistoryIterator";
import { Observer } from '../../ObserverPattern';
import  { User } from '../../../systemDataTypes/Entities';
import  { TimeEntry, Event } from '../../../systemDataTypes/SystemInternalData';


export class RentalsAndReturnsPerUser implements Observer {
    private users: Array<User>;
    private bikeFailedRentalsPerUser: Map<number, number>;
    private bikeSuccessfulRentalsPerUser: Map<number, number>;
    private bikeFailedReturnsPerUser: Map<number, number>;
    private bikeSuccessfulReturnsPerUser: Map<number, number>;
    
    private constructor() {
        this.bikeFailedRentalsPerUser = new Map<number, number>();
        this.bikeSuccessfulRentalsPerUser = new Map<number, number>();
        this.bikeFailedReturnsPerUser = new Map<number, number>();
        this.bikeSuccessfulReturnsPerUser = new Map<number, number>();
    }
    
    private async init(path: string): Promise<void> {
        let history: HistoryReader = await HistoryReader.create(path);
        try {
            let entities: HistoryEntitiesJson = await history.getEntities("users");
            this.users = entities.instances ;
                
            for(let user of this.users) {
                this.bikeFailedRentalsPerUser.set(user.id, 0);
                this.bikeSuccessfulRentalsPerUser.set(user.id, 0);
                this.bikeFailedReturnsPerUser.set(user.id, 0);            
                this.bikeSuccessfulReturnsPerUser.set(user.id, 0);            
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
            return rentalsAndReturnsValues;
        }
        catch(error) {
            console.log(error);
        }
    }

    public getBikeFailedRentalsOfUser(userId: number): number | undefined {
        return this.bikeFailedRentalsPerUser.get(userId);
    }
    
    public getBikeSuccessfulRentalsOfUser(userId: number): number | undefined {
        return this.bikeSuccessfulRentalsPerUser.get(userId);
    }
    
    public getBikeFailedReturnsOfUser(userId: number): number | undefined {
        return this.bikeFailedReturnsPerUser.get(userId);
    }
    
    public getBikeSuccessfulReturnsOfUser(userId: number): number | undefined {
        return this.bikeSuccessfulReturnsPerUser.get(userId);
    }
    
    public update(timeEntry: TimeEntry): void {
        let name: string;
        let event: Event;

        let key: number = undefined;
        let value: number | undefined = undefined;

        name = 'EventUserArrivesAtStationToRentBikeWithReservation';
        event = HistoryIterator.getEventByName(timeEntry, name);
        if (event !== undefined && event.changes.users.length !== 0) {
            key = event.changes.users[0].id;
            value = this.bikeSuccessfulRentalsPerUser.get(key);
            if (value !== undefined) {
                this.bikeSuccessfulRentalsPerUser.set(key, ++value);
            }
        }
        
        name = 'EventUserArrivesAtStationToReturnBikeWithReservation';
        event = HistoryIterator.getEventByName(timeEntry, name);
        if (event !== undefined &&  event.changes.users.length !== 0) {
            key = event.changes.users[0].id;
            value = this.bikeSuccessfulReturnsPerUser.get(key);
            if (value !== undefined) {
                this.bikeSuccessfulReturnsPerUser.set(key, ++value);
            }
        }
        
        name = 'EventUserArrivesAtStationToRentBikeWithoutReservation';
        event = HistoryIterator.getEventByName(timeEntry, name);
        if (event !== undefined && event.changes.users.length !== 0) {
            key = event.changes.users[0].id;
            let bike: any = event.changes.users[0].bike;
            if (bike !== undefined) {
                value = this.bikeSuccessfulRentalsPerUser.get(key);
                if (value !== undefined) {                
                    this.bikeSuccessfulRentalsPerUser.set(key, ++value);
                }
            }
            else {
                value = this.bikeFailedRentalsPerUser.get(key);
                if (value !== undefined) {
                    this.bikeFailedRentalsPerUser.set(key, ++value);
                }
            }
        }
        
        name = 'EventUserArrivesAtStationToReturnBikeWithoutReservation';
        event = HistoryIterator.getEventByName(timeEntry, name);
        if (event !== undefined && event.changes.users.length !== 0) {
            key = event.changes.users[0].id;
            let bike: any = event.changes.users[0].bike;
            if (bike !== undefined) {
                value = this.bikeSuccessfulReturnsPerUser.get(key);
                if (value !== undefined) {                  
                    this.bikeSuccessfulReturnsPerUser.set(key, ++value);
                }
            }
            else {
                value = this.bikeFailedReturnsPerUser.get(key);
                if (value !== undefined) {
                    this.bikeFailedReturnsPerUser.set(key, ++value);
                }
            }
        }
    }
       
}