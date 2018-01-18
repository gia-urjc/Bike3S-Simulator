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
        this.reservationsIterator.init(this.path).then(() => {
            this.counter++;
            this.verifyInicialization();     
        });
         
        this.reservationsPerUser = new ReservationsPerUser();
        this.reservationsPerUser.init(this.path).then( () => {
            this.counter++;
            this.verifyInicialization();
        });
        
        this.rentalsAndReturnsPerUser = new RentalsAndReturnsPerUser();
        this.rentalsAndReturnsPerUser.init(this.path).then( () => {
            this.counter++;
            this.verifyInicialization();
        });
        
        this.reservationsPerStation = new ReservationsPerStation();
        this.reservationsPerStation.init(this.path).then( () => { 
            this.counter++;
            this.verifyInicialization();
        });
        
        this.rentalsAndReturnsPerStation = new RentalsAndReturnsPerStation();
        this.rentalsAndReturnsPerStation.init(this.path).then( () => {
            this.counter++;
            this.verifyInicialization();
        });
    }
    
    private verifyInicialization() {
        if (this.counter === this.INICIALIZATION) {
            this.counter = 0;
            this.reservationsIterator.subscribe(this.reservationsPerUser);
            this.reservationsIterator.subscribe(this.reservationsPerStation);
            this.timeEntriesIterator.subscribe(this.rentalsAndReturnsPerUser);        
            this.timeEntriesIterator.subscribe(this.rentalsAndReturnsPerStation);
            
            this.reservationsIterator.calculateReservations().then( () => {
                this.counter++;
                this.verifyCalculation();
            });
            this.timeEntriesIterator.calculateBikeRentalsAndReturns(this.path).then( () => {
                this.counter++;
                this.verifyCalculation();
            });
         }
    }
    
    private verifyCalculation() {
        if (this.counter === this.CALCULATION) {
            console.log('Failed rental attempts in sation 4:', this.rentalsAndReturnsPerStation.getBikeFailedRentalsOfStation(4));
        }
    }

    public static async create(path: string) {
        let generator: DataGenerator = new DataGenerator(path);
        await generator.init();
        return generator;
    }
    
}