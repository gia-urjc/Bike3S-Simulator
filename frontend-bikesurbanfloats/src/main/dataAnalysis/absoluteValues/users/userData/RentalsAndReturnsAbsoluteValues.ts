import { HistoryReader } from '../../../../util';
import { HistoryEntities } from '../../../../../shared/history';
import { Observer } from '../../ObserverPattern';
import  { User } from '../../../dataTypes/Entities';
import  { TimeEntry } from '../../../dataTypes/SystemInternalData';
import { BikeFailedRentals, BikeSuccessfulRentals,
    BikeFailedReturns, BikeSuccessfulReturns } from '../userDataDefinitions/RentalsAndReturnsPerUser';    

export class RentalsAndReturnsAbsoluteValues implements Observer {
    private users: Array<User>;
    private bikeFailedRentalsPerUser: BikeFailedRentals;
    private bikeSuccessfulRentalsPerUser: BikeSuccessfulRentals;
    private bikeFailedReturnsPerUser: BikeFailedReturns;
    private bikeSuccessfulReturnsPerUser: BikeSuccessfulReturns;
    
    private constructor() {
        this.bikeFailedRentalsPerUser = new BikeFailedRentals();
        this.bikeSuccessfulRentalsPerUser = new BikeSuccessfulRentals();
        this.bikeFailedReturnsPerUser = new BikeFailedReturns();
        this.bikeSuccessfulReturnsPerUser = new BikeSuccessfulReturns();
    }
    
    private async init(path: string): Promise<void> {
        let history: HistoryReader = await HistoryReader.create(path);
        let entities = await history.readEntities();
        this.users = entities.users;
        
        for(let user of this.users) {
            this.bikeFailedRentalsPerUser.getMap().set(user.id, 0);
            this.bikeSuccessfulRentalsPerUser.getMap().set(user.id, 0);
            this.bikeFailedReturnsPerUser.getMap().set(user.id, 0);            
            this.bikeSuccessfulReturnsPerUser.getMap().set(user.id, 0);            
        }
    }

    public static async create(path: string): Promise<RentalsAndReturnsAbsoluteValues> {
        let rentalsAndReturnsValues = new RentalsAndReturnsAbsoluteValues();
        await rentalsAndReturnsValues.init(path);
        return rentalsAndReturnsValues;
    }

    public getBikeFailedRentalsOfUser(userId: number): number | undefined {
        return this.bikeFailedRentalsPerUser.getMap().get(userId);
    }
    
    public getBikeSuccessfulRentalsOfUser(userId: number): number | undefined {
        return this.bikeSuccessfulRentalsPerUser.getMap().get(userId);
    }
    
    public getBikeFailedReturnsOfUser(userId: number): number | undefined {
        return this.bikeFailedReturnsPerUser.getMap().get(userId);
    }
    
    public getBikeSuccessfulReturnsOfUser(userId: number): number | undefined {
        return this.bikeSuccessfulReturnsPerUser.getMap().get(userId);
    }
    
    public update(timeEntry: TimeEntry) {
        this.bikeFailedRentalsPerUser.update(timeEntry);
        this.bikeSuccessfulRentalsPerUser.update(timeEntry);
        this.bikeFailedReturnsPerUser.update(timeEntry);
        this.bikeSuccessfulReturnsPerUser.update(timeEntry);
    }
    
}