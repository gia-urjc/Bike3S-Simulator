import { Data } from '../absoluteValues/Data';
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
    private calculators: Map<string, Calculator>;
    private data: Map<string, Data>;
    
    public constructor(path: string) {
        this.path = path;
        this.counter = 0;
        this.calculators = new Map(); 
        this.data = new Map();
    }
    
    private async generate(): Promise<void> {
        this.calculators.set(ReservationCalculator.name, new ReservationCalculator());
                this.calculators.set(RentalAndReturnCalculator.name, new RentalAndReturnCalculator());
        
        this.data.set(ReservationsPerUser.name, new ReservationsPerUser());
        this.data.set(RentalsAndReturnsPerUser.name, new RentalsAndReturnsPerUser());
        this.data.set(ReservationsPerStation.constructor.name, new ReservationsPerStation());
        this.data.set(RentalsAndReturnsPerStation.name, new RentalsAndReturnsPerStation());
        
        this.data.forEach( (value, key) => {
            if (value.actionType == 'reservation') {
                this.reservationCalculator.subscribe(value);
            }
            else if (value.actionType === 'rentalAndReturn') {
                this.rentalAndReturnCalculator.subscribe(value);
            }
        });
        
        this.calculators.get(ReservationCalculator.name).init(this.path).then( () => {
            this.counter++; 
            this.calculateAbsoluteValues();     
        });
        
        this.data.forEach( (value, key) => {
            value.init(this.path).then( () => {
                this.counter++; 
                this.calculateAbsoluteValues();
            });
        });
    }
    
    private async calculateAbsoluteValues(): Promise<void> {
        if (this.counter === this.INICIALIZATION) {
            this.counter = 0;
            this.calculators.forEach( (value, key)) => {
                value.calculate().then( () => {
                    this.counter++;
                    this.write();
                });
            });
            
         }
    }
    
    public static async create(path: string): Promise<DataGenerator> {
        let generator: DataGenerator = new DataGenerator(path);
        try {
        await generator.generate();
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

		public getData(): Map<string, Data> {
		return this.data;
	}
     
}