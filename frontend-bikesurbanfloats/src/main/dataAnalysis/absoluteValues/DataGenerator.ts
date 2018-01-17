import { ReservationsIterator } from './systemDataIterators/ReservationsIterator';
import { TimeEntriesIterator } from './systemDataIterators/TimeEntriesIterator';
import { ReservationsPerUser } from './analysisData/users/ReservationsPerUser';
import { RentalsAndReturnsPerUser } from './analysisData/users/RentalsAndReturnsPerUser';
import { ReservationsPerStation } from './analysisData/stations/ReservationsPerStation';
import { RentalsAndReturnsPerStation } from './analysisData/stations/RentalsAndReturnsPerStation';

export class DataGenerator {
    private path: string;
    private counter: number;
    private reservationsIterator: ReservationsIterator;
    private timeEntriesIterator: TimeEntriesIterator;
    private reservationsPerUser: ReservationsPerUser;
    private rentalsAndReturnsPerUser: RentalsAndReturnsPerUser;
    private reservationsPerStation: ReservationsPerStation;
    private rentalsAndReturnsPerStation: RentalsAndReturnsPerStation;  
    
    public constructor(path: string) {
        this.path = path;
        this.counter = 0;
    }
    
    public async init(): Promise<void> {
        this.reservationsIterator = new ReservationsIterator();
        this.reservationsIterator.init().then(() => {
            this.counter++;
            verify();     
        })
        this.timeEntriesIterator = await TimeEntriesIterator.create();
        this.reservationsPerUser = await ReservationsPerUser.create(this.path);
        this.rentalsAndReturnsPerUser = await RentalsAndReturnsPerUser.create(this.path);
        this.reservationsPerStation = await ReservationsPerStation.create(this.path);
        this.rentalsAndReturnsPerStation = await RentalsAndReturnsPerStation.create(this.path);
        
        this.reservationsIterator.subscribe(this.reservationsPerUser);
        this.reservationsIterator.subscribe(this.reservationsPerStation);
        this.timeEntriesIterator.subscribe(this.rentalsAndReturnsPerUser);        
        this.timeEntriesIterator.subscribe(this.rentalsAndReturnsPerStation);
        
        this.reservationsIterator.calculateReservations();
        this.timeEntriesIterator.calculateBikeRentalsAndReturns(this.path);
    }
    
    }

    
}