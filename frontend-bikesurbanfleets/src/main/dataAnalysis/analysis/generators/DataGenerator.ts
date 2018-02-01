
import { RentalsAndReturnsPerStation } from "../absoluteValues/rentalsAndReturns/RentalsAndReturnsPerStation";
import { RentalsAndReturnsPerUser } from "../absoluteValues/rentalsAndReturns/RentalsAndReturnsPerUser";
import { ReservationsPerStation } from "../absoluteValues/reservations/ReservationsPerStation";
import { ReservationsPerUser } from "../absoluteValues/reservations/ReservationsPerUser";
import { ReservationCalculator } from "../systemDataCalculators/ReservationCalculator";
import { RentalAndReturnCalculator } from "../systemDataCalculators/RentalAndReturnCalculator";
import { CsvGenerator } from "./CsvGenerator";

export class DataGenerator {
    private readonly INICIALIZATION: number = 5;
    private readonly CALCULATION: number = 2;
    private path: string;
    private counter: number;
    private reservationCalculator: ReservationCalculator;
    private rentalAndReturnCalculator: RentalAndReturnCalculator;
    private data: Map<string, any>;
    
    public constructor(path: string) {
        this.path = path;
        this.counter = 0;
        this.rentalAndReturnCalculator = new RentalAndReturnCalculator();
        this.reservationCalculator = new ReservationCalculator();
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
        
        this.reservationCalculator.subscribe(reservationsPerUser);
        this.reservationCalculator.subscribe(reservationsPerStation);
        this.rentalAndReturnCalculator.subscribe(rentalsAndReturnsPerUser);        
        this.rentalAndReturnCalculator.subscribe(rentalsAndReturnsPerStation);
       
        this.reservationCalculator.init(this.path).then( () => {
            this.counter++; 
            this.calculateAbsoluteValues();     
        });
         
        reservationsPerUser.init(this.path).then( () => {
            this.counter++; 
            this.calculateAbsoluteValues();
        });
        
        rentalsAndReturnsPerUser.init(this.path).then( () => {
            this.counter++;        
            this.calculateAbsoluteValues();
        });
        
        reservationsPerStation.init(this.path).then( () => { 
            this.counter++;
            this.calculateAbsoluteValues();
        });
        
       rentalsAndReturnsPerStation.init(this.path).then( () => {
            this.counter++;
            this.calculateAbsoluteValues();
        });
    }
    
    private async calculateAbsoluteValues(): Promise<void> {
        if (this.counter === this.INICIALIZATION) {
            this.counter = 0;
            this.reservationCalculator.calculateReservations().then( () => {
                this.counter++;
                this.write();
            });
            this.rentalAndReturnCalculator.calculateBikeRentalsAndReturns(this.path).then( () => {
                this.counter++;
                this.write();
            });
         }
    }
    
    public static async generate(path: string): Promise<DataGenerator> {
        let generator: DataGenerator = new DataGenerator(path);
        try {
        await generator.init();
        }
        catch(error) {
            console.log('error initializing data generator:', error);
        }
        return generator;
    }
    
    private async write(): Promise<void> {
        if (this.counter === this.CALCULATION) {
          let generator: CsvGenerator = new CsvGenerator(this.path);
          await generator.generate(this.data);
        }
      return;
    }

		public Data(): Map<string, any> {
		return this.data;
	}
     
}