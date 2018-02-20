import { Info } from '../absoluteValues/Info';
import { RentalsAndReturnsPerStation } from "../absoluteValues/rentalsAndReturns/RentalsAndReturnsPerStation";
import { RentalsAndReturnsPerUser } from "../absoluteValues/rentalsAndReturns/RentalsAndReturnsPerUser";
import { ReservationsPerStation } from "../absoluteValues/reservations/ReservationsPerStation";
import { ReservationsPerUser } from "../absoluteValues/reservations/ReservationsPerUser";
import { Calculator } from "../systemDataCalculators/Calculator";
import { ReservationCalculator } from "../systemDataCalculators/ReservationCalculator";
import { RentalAndReturnCalculator } from "../systemDataCalculators/RentalAndReturnCalculator";
import { CsvGenerator } from "./CsvGenerator";
import { Entity } from '../../systemDataTypes/Entities';

export class DataGenerator {
    private readonly RESERVVATION: number = 2;
    private readonly RENTAL_AND_RETURN: number = 3;

    private boolean csv;    
    private infoTypes: Array<Info>;  // It indicates the types of info. the analizer must calculate

    private path: string;
    private reservationCounter: number;
    private rentalAndReturnCounter: number;
   
    private data: Map<string, Info>;
    private reservationCalculator: ReservationCalculator;
    private rentalAndReturnCalculator: RentalAndReturnCalculator;

    public constructor(path: string, csv: boolean) {
        this.csv = csv;
        this.path = path;
        this.reservationCounter = 0;
        this.rentalAndReturnCounter = 0;
        this.data = new Map();
    }

    private async initReservations(reservations: Info) {
        reservations.init().then( () => {
            this.reservationCounter++;
            this.calculateReservations();
        });
    }

    public async initRentalsAndReturns(rentalsAndReturns: Info) {
        rentalsAndReturns.init().then( () => {
            this.rentalAndReturnCounter++;
            this.calculateRentalsAndReturns();
        });
    }

    pubblic async generate(): Promise<void> {
        this.calculators.set(RentalAndReturnCalculator.name, new RentalAndReturnCalculator());

        let systemReservations: SystemReservationsInfo = new SystemReservationsInfo();
        systemReservations.init().then( () => { 
            let emptyStations: EmptyStationsInfo = new EmptyStationsInfo(systemReservations.getReservations());
            this.rentalAndReturnCalculator.subscribe(emptyStations);          
            this.data.set(EmptyStationsInfo.name, emptyStations);
            this.reservationCalculator = new ReservationCalculator(systemReservations.getReservations());
        }); 

        let systemStations: SystemStationsInfo = new SystemStationsInfo();
        systemStations.init().then( () = > {
            let reservations: ReservationsPerStation = new ReservationsPerStation(systemStations.getStations());
            this.reservationCalculator.subscribe(reservations);
            this.data.set(ReservationsPerStation.name, reservations);
            this.initReservations(reservations);

            let rentalsAndReturns: RentalsAndReturnsPerStation = new RentalsAndReturnsPerStation(systemStations.getStations()); 
            this.rentalAndReturnCalculator.subscribe(rentalsAndReturns);
            this.data.set(RentalsAndReturnsPerStation.name, rentalsAndReturns);  
            this.initRentalsAndReturns(rentalsAndReturns);
        });

        let systemUsers: SystemUsersInfo = new SystemUsersInfo(); 
        systemUsers.init().then( () => {
            let reservations: ReservationsPerUser = new ReservationsPerUser(systemUsers.getUsers());
            this.reservationCalculator.subscribe(reservations);
            this.data.set(ReservationsPerUser.name, reservations);
            this.initReservations(reservations);

            let rentalsAndReturns: RentalsAndReturnsPerUser = new RentalsAndReturnsPerUser(systemUsers.getUsers()); 
            this.rentalAndReturnCalculator.subscribe(rentalsAndReturns);
            this.data.set(RentalsAndReturnsPerUser.name, rentalsAndReturns);  
            this.initRentalsAndReturns(rentalsAndReturns);
        }); 
    }

    private async calculateReservations(): Promise<void> {
        if (reservationCounter === RESERVATIONS) {
      // TODO: calculate must be async
            this.reservationCalculator.calculate();
        }
    }

    private async calculateRentalsAndReturns(): Promise<void> {
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