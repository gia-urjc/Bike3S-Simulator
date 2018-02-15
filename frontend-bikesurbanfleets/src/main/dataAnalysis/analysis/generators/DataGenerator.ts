import { Info } from '../absoluteValues/Info';
import { RentalsAndReturnsPerStation } from "../absoluteValues/rentalsAndReturns/RentalsAndReturnsPerStation";
import { RentalsAndReturnsPerUser } from "../absoluteValues/rentalsAndReturns/RentalsAndReturnsPerUser";
import { ReservationsPerStation } from "../absoluteValues/reservations/ReservationsPerStation";
import { ReservationsPerUser } from "../absoluteValues/reservations/ReservationsPerUser";
import { Calculator } from "../systemDataCalculators/Calculator";
import { ReservationCalculator } from "../systemDataCalculators/ReservationCalculator";
import { RentalAndReturnCalculator } from "../systemDataCalculators/RentalAndReturnCalculator";
import { CsvGenerator } from "./CsvGenerator";

export class DataGenerator {
    private boolean csv;
    private readonly INICIALIZATION: number = 5;
    private readonly CALCULATION: number = 2;
    
    private path: string;
    private counter: number;
    
    private calculators: Map<string, Calculator>;
    private data: Map<string, Info>;
    
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
        
        this.data.forEach( (info, key) => {
            switch (info.getFactType()) {
                case: 'RESERVATION': {
                    this.calculators.get(ReservationCalculator.name).subscribe(info);
                    break;
                }
                    
                case 'RENTAL_AND_RETURN':
                case 'EMPTY_STATION': {
                    this.calculators.get(RentalAndReturnCalculator.name).subscribe(info);
                    break;
                }
            }
        });
        
        this.calculators.get(ReservationCalculator.name).init(this.path).then( () => {
            this.counter++; 
            this.calculateAbsoluteValues();     
        });
        
        this.data.forEach( (info, key) => {
            info.init(this.path).then( () => {
                this.counter++; 
                this.calculateAbsoluteValues();
            });
        });
    }
    
    private async calculateAbsoluteValues(): Promise<void> {
        if (this.counter === this.INICIALIZATION) {
            this.counter = 0;
            this.calculators.forEach((calculator, key) => {
                calculator.calculate().then(() => {
                    this.counter++;
                    if (csv) {
                        this.write();
                    }
                })
            });
            
         }
        return;
    }
    
    public static async create(path: string): Promise<DataGenerator> {
        let generator: DataGenerator = new DataGenerator(path);
        try {
        await generator.generate();
        }
        catch(error) {
            throw new Error('Error initializing data generator: '+error);
        }
        return generator;
    }
    
    private async write(): Promise<void> {
        if (this.counter === this.CALCULATION) {
          let generator: CsvGenerator = new CsvGenerator(this.path);
          try {
              await generator.generate(this.data);
          }
          catch(error) {
              throw new Error('Error generating csv file: '+error);
          }
          
      return;
    }

		public getData(): Map<string, Info> {
		return this.data;
	}
     
}