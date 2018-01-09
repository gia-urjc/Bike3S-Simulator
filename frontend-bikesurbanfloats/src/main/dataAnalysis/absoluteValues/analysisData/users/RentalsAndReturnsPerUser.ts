import { HistoryReader } from '../../../../util';
import { HistoryEntitiesJson } from '../../../../../shared/history';
import { Observer } from '../../ObserverPattern';
import  { User } from '../../../systemDataTypes/Entities';
import  { TimeEntry } from '../../../systemDataTypes/SystemInternalData';

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
        let entities: HistoryEntitiesJson = await history.getEntities("users");
        this.users = entities.instances ;
                
        for(let user of this.users) {
            this.bikeFailedRentalsPerUser.set(user.id, 0);
            this.bikeSuccessfulRentalsPerUser.set(user.id, 0);
            this.bikeFailedReturnsPerUser.set(user.id, 0);            
            this.bikeSuccessfulReturnsPerUser.set(user.id, 0);            
        }
    }

    public static async create(path: string): Promise<RentalsAndReturnsPerUser> {
        let rentalsAndReturnsValues = new RentalsAndReturnsPerUser();
        await rentalsAndReturnsValues.init(path);
        return rentalsAndReturnsValues;
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
    
    public update(timeEntry: TimeEntry) {
    }
    
}