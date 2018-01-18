import { ReservationsIterator } from './systemDataIterators/ReservationsIterator';
import { TimeEntriesIterator } from './systemDataIterators/TimeEntriesIterator';
import { ReservationsPerUser } from './analysisData/users/ReservationsPerUser';
import { RentalsAndReturnsPerUser } from './analysisData/users/RentalsAndReturnsPerUser';
import { ReservationsPerStation } from './analysisData/stations/ReservationsPerStation';
import { RentalsAndReturnsPerStation } from './analysisData/stations/RentalsAndReturnsPerStation';

export class DataGenerator {
    private readonly INICIALIZATION: number = 6;
    private readonly CALCULATION: number = 2;
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
        this.timeEntriesIterator = new TimeEntriesIterator();
        this.reservationsIterator = new ReservationsIterator();
        this.reservationsPerUser = new ReservationsPerUser();
        this.rentalsAndReturnsPerUser = new RentalsAndReturnsPerUser();
        this.reservationsPerStation = new ReservationsPerStation();
        this.rentalsAndReturnsPerStation = new RentalsAndReturnsPerStation();
        
        this.reservationsIterator.subscribe(this.reservationsPerUser);
        this.reservationsIterator.subscribe(this.reservationsPerStation);
        this.timeEntriesIterator.subscribe(this.rentalsAndReturnsPerUser);        
        this.timeEntriesIterator.subscribe(this.rentalsAndReturnsPerStation);
       
        this.reservationsIterator.init(this.path).then(() => {
            this.counter++;
            this.calculateAbsoluteValues();     
        });
         
        this.reservationsPerUser.init(this.path).then( () => {
            this.counter++;
            this.calculateAbsoluteValues();
        });
        
        this.rentalsAndReturnsPerUser.init(this.path).then( () => {
            this.counter++;
            this.calculateAbsoluteValues();
        });
        
        this.reservationsPerStation.init(this.path).then( () => { 
            this.counter++;
            this.calculateAbsoluteValues();
        });
        
       this.rentalsAndReturnsPerStation.init(this.path).then( () => {
            this.counter++;
            this.calculateAbsoluteValues();
        });
    }
    
    private calculateAbsoluteValues() {
        if (this.counter === this.INICIALIZATION) {
            this.reservationsIterator.calculateReservations();
            this.timeEntriesIterator.calculateBikeRentalsAndReturns(this.path);
         }
    }
    
    public static async create(path: string) {
        let generator: DataGenerator = new DataGenerator(path);
        await generator.init();
        return generator;
    }
    
}