import * as csv_parser from 'json2csv';
import { ReservationsIterator } from './systemDataIterators/ReservationsIterator';
import { TimeEntriesIterator } from './systemDataIterators/TimeEntriesIterator';
import { ReservationsPerUser } from './analysisData/users/ReservationsPerUser';
import { RentalsAndReturnsPerUser } from './analysisData/users/RentalsAndReturnsPerUser';
import { ReservationsPerStation } from './analysisData/stations/ReservationsPerStation';
import { RentalsAndReturnsPerStation } from './analysisData/stations/RentalsAndReturnsPerStation';

export class DataGenerator {
    private readonly INICIALIZATION: number = 5;
    private readonly CALCULATION: number = 2;
    private path: string;
    private counter: number;
    private reservationsIterator: ReservationsIterator;
    private timeEntriesIterator: TimeEntriesIterator;
    private data: Map<string, any>;
    
    public constructor(path: string) {
        this.path = path;
        this.counter = 0;
        this.timeEntriesIterator = new TimeEntriesIterator();
        this.reservationsIterator = new ReservationsIterator();
        this.data = new Map();
    }
    
    private async init(): Promise<void> {
        let reservationsPerUser: ReservationsPerUser = new ReservationsPerUser();
        let rentalsAndReturnsPerUser: RentalsAndReturnsPerUser = new RentalsAndReturnsPerUser();
        let reservationsPerStation: ReservationsPerStation = new ReservationsPerStation();
        let rentalsAndReturnsPerStation: RentalsAndReturnsPerStation = new RentalsAndReturnsPerStation();
        
        this.data.set(reservationsPerUser.constructor.name, reservationsPerUser);
        this.data.set(rentalsAndReturnsPerUser.constructor.name, rentalsAndReturnsPerUser);
        this.data.set(reservationsPerStation.constructor.name, reservationsPerStation);
        this.data.set(rentalsAndReturnsPerStation.constructor.name, rentalsAndReturnsPerStation);
        
        this.reservationsIterator.subscribe(reservationsPerUser);
        this.reservationsIterator.subscribe(reservationsPerStation);
        this.timeEntriesIterator.subscribe(rentalsAndReturnsPerUser);        
        this.timeEntriesIterator.subscribe(rentalsAndReturnsPerStation);
       
        this.reservationsIterator.init(this.path).then( () => {
            this.counter++;        console.log('iterator:',this.counter); 
            this.calculateAbsoluteValues();     
        });
         
        reservationsPerUser.init(this.path).then( () => {
            this.counter++;        console.log('RPU:',this.counter);
            this.calculateAbsoluteValues();
        });
        
        rentalsAndReturnsPerUser.init(this.path).then( () => {
            this.counter++;        console.log('RARPU:',this.counter);
            this.calculateAbsoluteValues();
        });
        
        reservationsPerStation.init(this.path).then( () => { 
            this.counter++;        console.log('RPT:',this.counter);
            this.calculateAbsoluteValues();
        });
        
       rentalsAndReturnsPerStation.init(this.path).then( () => {
            this.counter++;        console.log('RARPT:',this.counter);
            this.calculateAbsoluteValues();
        });
    }
    
    private calculateAbsoluteValues(): void {
        if (this.counter === this.INICIALIZATION) {
            console.log('INICIALIZADO TODO:',this.counter);
        
            this.counter = 0;
            this.reservationsIterator.calculateReservations().then( () => {
                this.counter++; console.log('reservations calculated', this.counter);
                this.write();
            });
            this.timeEntriesIterator.calculateBikeRentalsAndReturns(this.path).then( () => {
                this.counter++;console.log('rentals and returns calculated', this.counter);
                this.write();
            });
         }
    }
    
    public static async create(path: string): Promise<DataGenerator> {
        let generator: DataGenerator = new DataGenerator(path);
        try {
        await generator.init();
        }
        catch(error) {
            console.log('error initializing data generator:', error);
        }
        return generator;
    }
    
    private write(): void {
        if (this.counter === this.CALCULATION) {
          this.data.forEach( (value, key) => value.print());
        }
    }
       
}