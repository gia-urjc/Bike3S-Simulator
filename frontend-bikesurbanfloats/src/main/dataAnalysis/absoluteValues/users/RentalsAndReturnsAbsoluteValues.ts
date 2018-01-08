import { HistoryReader } from '../../../util';
import { Observer } from '../ObserverPattern';
import  { User } from '../../dataTypes/Entities';
import  { TimeEntry } from '../../dataTypes/SystemInternalData';

export class RentalsAndReturnsAbsoluteValues implements Observer {
    private users: Array<User>;
    private bikeFailedRentalsPerUser: Map<number, number>;
    private bikeSuccessfulRentalsPerUser: Map<number, number>;
    private bikeFailedReturnsPerUser: Map<number, number>;
    private bikeSuccessfulReturnsPerUser: Map<number, number>;
    
    private constructor() {
        this.bikeFailedRentalsPerUser = new Map<number, number>();
        this.bikeSuccessfulRentalsPerUser = new Map<number, number>();
        this.bikeFailedReturnsPerUser = new Map<number, number>;
        this.bikeSuccessfulReturnsPerUser = new Map<number, number>;
    }
    
    private async init(path: string): Promise<void> {
        let history: HistoryReader = await HistoryReader.create(path);
        this.users = await history.getEntities("users").instaces;
                
        for(let user of this.users) {
            this.bikeFailedRentalsPerUser.set(user.id, 0);
            this.bikeSuccessfulRentalsPerUser.set(user.id, 0);
            this.bikeFailedReturnsPerUser.set(user.id, 0);            
            this.bikeSuccessfulReturnsPerUser.set(user.id, 0);            
        }
    }

    public static async create(path: string): Promise<RentalsAndReturnsAbsoluteValues> {
        let rentalsAndReturnsValues = new RentalsAndReturnsAbsoluteValues();
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