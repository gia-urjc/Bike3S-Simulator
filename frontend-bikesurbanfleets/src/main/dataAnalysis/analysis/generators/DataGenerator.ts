import { Info } from '../absoluteValues/Info';
import { RentalsAndReturnsPerStation } from "../absoluteValues/rentalsAndReturns/RentalsAndReturnsPerStation";
import { RentalsAndReturnsPerUser } from "../absoluteValues/rentalsAndReturns/RentalsAndReturnsPerUser";
import { ReservationsPerStation } from "../absoluteValues/reservations/ReservationsPerStation";
import { ReservationsPerUser } from "../absoluteValues/reservations/ReservationsPerUser";
import { ReservationCalculator } from "../calculators/ReservationCalculator";
import { RentalAndReturnCalculator } from "../calculators/RentalAndReturnCalculator";
import { SystemReservations } from "../systemEntities/SystemReservations";
import { SystemStations } from "../systemEntities/SystemStations";
import { CsvGenerator } from "./CsvGenerator";

export class DataGenerator {
    private readonly RESERVVATIONS: number = 3;  // 3 data related to reservations must be initialized
    private readonly RENTALS_AND_RETURNS: number = 3;  // 3 data related to rentals and returns must be initialized
        private readonly CALCULATION: number = 2;  // both calculators must have calculated its data before writing them 

    private csv: boolean;  // it indicates if a csv file must be generated    
    private path: string;  // it's the path of history files
    private reservationCounter: number;  // number of data related to reservations which have been initialized
    private rentalAndReturnCounter: number;  // number of data related to rentals and returns which have been initialized
    private calculationCounter: number;  // number of calculators which have calculated its data
   
    private data: Map<string, Info>;  // it contains all the results of the data analysis
    private reservationCalculator: ReservationCalculator;
    private rentalAndReturnCalculator: RentalAndReturnCalculator;

    public constructor(path: string, csv: boolean) {
        this.csv = csv;
        this.path = path;
        this.reservationCounter = 0;
        this.rentalAndReturnCounter = 0;
        this.data = new Map();
        this.rentalAndReturnCalculator = new RentalAndReturnCalculator();
        this.reservationCalculator = new ReservationCalculator();
    }

    private async initReservations(reservations: Info) {
        reservations.init().then( () => {
            this.reservationCounter++;
            this.calculateReservations();
        });
        return;
    }

    private async initRentalsAndReturns(rentalsAndReturns: Info) {
        rentalsAndReturns.init().then( () => {
            this.rentalAndReturnCounter++;
            this.calculateRentalsAndReturns();
        });
        return;
    }

    public async generate(): Promise<void> {
        let bikesPerStation: BikesPerStation; 

        let systemReservations: SystemReservations = new SystemReservations();
        systemReservations.init(this.path).then( () => { 
            bikesPerStation = new BikesPerStation(systemReservations.getReservations());
            this.rentalAndReturnCalculator.subscribe(emptyStations);          
            this.data.set(EmptyStationsInfo.name, emptyStations);
            
            this.reservationCalculator.setReservations(systemReservations.getReservations());
            this.reservationCounter++;
            this.calculateReservations();
        });
             
       let systemStations: SystemStations = new SystemStations();
        systemStations.init(this.path).then( () => {
            let reservations: ReservationsPerStation = new ReservationsPerStation(systemStations.getStations());
            this.reservationCalculator.subscribe(reservations);
            this.data.set(ReservationsPerStation.name, reservations);
            this.initReservations(reservations);  // it's async

            let rentalsAndReturns: RentalsAndReturnsPerStation = new RentalsAndReturnsPerStation(systemStations.getStations()); 
            this.rentalAndReturnCalculator.subscribe(rentalsAndReturns);
            this.data.set(RentalsAndReturnsPerStation.name, rentalsAndReturns);  
            this.initRentalsAndReturns(rentalsAndReturns);  // it's async
            
            bikesPerStation.init(systemStations.getStations()).then( () => {
                this.rentalAndReturnCounter++;
                this.calculateRentalsAndReturns();
            });
        });

        let systemUsers: SystemUsersInfo = new SystemUsersInfo(); 
        systemUsers.init(this.path).then( () => {
            let reservations: ReservationsPerUser = new ReservationsPerUser(systemUsers.getUsers());
            this.reservationCalculator.subscribe(reservations);
            this.data.set(ReservationsPerUser.name, reservations);
            this.initReservations(reservations);

            let rentalsAndReturns: RentalsAndReturnsPerUser = new RentalsAndReturnsPerUser(systemUsers.getUsers()); 
            this.rentalAndReturnCalculator.subscribe(rentalsAndReturns);
            this.data.set(RentalsAndReturnsPerUser.name, rentalsAndReturns);  
            this.initRentalsAndReturns(rentalsAndReturns);
        });
        return; 
    }

    private async calculateReservations(): Promise<void> {
        if (this.reservationCounter === this.RESERVATIONS) {
            this.reservationCalculator.calculate().then( () => {
                this.writeCounter++;
                this.write();
            });
        }
        return;
    }

    private async calculateRentalsAndReturns(): Promise<void> {
        if (this.rentalAndReturnCounter === this.RENTALS_AND_RETURNS) {
            this.rentalAndReturnCalculator.calculate().then( () => {
                if (csv) {
                    this.writeCounter++;
                    this.write();
                }
            });
        }
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
        if (this.calculationCounter === this.CALCULATION) {
          let generator: CsvGenerator = new CsvGenerator(this.path);
          try {
              await generator.generate(this.data);
          }
          catch(error) {
              throw new Error('Error generating csv file: '+error);
          }
        }
        return;
    }

    public getData(): Map<string, Info> {
		return this.data;
	}
     
}