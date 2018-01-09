import { ReservationsIterator, TimeEntriesIterator } from './systemDataIterators/ReservationsIterator';
import { ReservationsPerUser } from './analysisData/users/ReservationsPerUser';
import { RentalsAndReturnsPerUser } from './analysisData/users/RentalsAndReturnsPerUser';
import { ReservationsPerStation } from './analysisData/stations/ReservationsPerStation';
import { RentalsAndReturnsPerStation } from './analysisData/stations/RentalsAndReturnsPerStation';

export class DataGenerator {
    private path: string;
    private reservationsIterator: ReservationsIterator;
    private timeEntriesIterator: TimeEntriesIterator;
    private reservationsPerUser: ReservationsPerUser;
    private rentalsAndReturnsPerUser: RentalsAndReturnsPerUser;
    
    public constructor(path: string) {
        this.path = path;
    }
    
    public async init(): Promise<void> {
        this.reservationsIterator = await ReservationsIterator.create(this.path);
        this.timeEntriesIterator = await TimeEntriesIterator.create();
        this.reservationsPerUser = await ReservationsPerUser.create(this.path);
        
    }
    
}