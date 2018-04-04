import { HistoryReader } from "../../../util";
import { Data } from '../absoluteValues/Data';
import { SystemInfo } from "../absoluteValues/SystemInfo";
import { RentalsAndReturnsPerStation } from "../absoluteValues/rentalsAndReturns/RentalsAndReturnsPerStation";
import { RentalsAndReturnsPerUser } from "../absoluteValues/rentalsAndReturns/RentalsAndReturnsPerUser";
import { ReservationsPerStation } from "../absoluteValues/reservations/ReservationsPerStation";
import { ReservationsPerUser } from "../absoluteValues/reservations/ReservationsPerUser";
import { BikesPerStation } from "../absoluteValues/time/BikesPerStation";
import { EmptyStationInfo } from "../absoluteValues/time/EmptyStationInfo";
import { ReservationCalculator } from "../calculators/ReservationCalculator";
import { RentalAndReturnCalculator } from "../calculators/RentalAndReturnCalculator";
import { SystemReservations } from "../systemEntities/SystemReservations";
import { SystemStations } from "../systemEntities/SystemStations";
import { SystemUsers } from "../systemEntities/SystemUsers";
import { CsvGenerator } from "./CsvGenerator";
import {SystemGlobalInfo } from '../SystemGlobalInfo';

export class DataGenerator {
    private readonly RESERVATIONS: number = 3;  // 3 data related to reservations must be initialized
    private readonly RENTALS_AND_RETURNS: number = 2;  // 2 data related to rentals and returns must be initialized
    private readonly CALCULATION: number = 2;  // both calculators must have calculated its data before writing them 
    private readonly BIKES_PER_STATION: number = 2;  // this data needs to be initialized with both reservation and station information

    private csv: boolean;  // it indicates if a csv file must be generated    
    private historyPath: string;  
    private schemaPath?: string;
    private csvPath?: string;
    
    private reservationCounter: number;  // number of data related to reservations which have been initialized
    private rentalAndReturnCounter: number;  // number of data related to rentals and returns which have been initialized
    private calculationCounter: number;  // number of calculators which have calculated its data
    private bikesPerStationCounter: number;
     
    private history: HistoryReader;
    private info: Map<string, SystemInfo>;  // it contains all the results of the data analysis
    private bikesPerStation: BikesPerStation;
    private reservationCalculator: ReservationCalculator;
    private rentalAndReturnCalculator: RentalAndReturnCalculator;

    private constructor(historyPath: string, csvPath?: string, schemaPath?: string) {
        this.csv = csvPath == undefined ? false: true;
        this.historyPath = historyPath;
        this.schemaPath = schemaPath == undefined ? undefined : schemaPath;
        this.csvPath = csvPath == undefined ? undefined : csvPath;
        this.reservationCounter = 0;
        this.rentalAndReturnCounter = 0;
        this.calculationCounter = 0;
        this.bikesPerStationCounter = 0;
        this.info = new Map();
        this.bikesPerStation = new BikesPerStation();
        this.rentalAndReturnCalculator = new RentalAndReturnCalculator();
        this.reservationCalculator = new ReservationCalculator();
    }
    
    private async init(): Promise<void> {
        try {
            this.history = await HistoryReader.create(this.historyPath, schemaPath);
            this.rentalAndReturnCalculator.setHistory(this.history);
        }
        catch(error) {
            throw new Error('Error reading history file: '+error);
        }
        return;
    } 

    private async initReservations(reservations: SystemInfo) {
        reservations.init().then( () => {
            this.reservationCounter++;
            this.calculateReservations();
        });
        return;
    }

    private async initRentalsAndReturns(rentalsAndReturns: SystemInfo) {
        rentalsAndReturns.init().then( () => {
            this.rentalAndReturnCounter++;
            this.calculateRentalsAndReturns();
        });
        return;
    }

    public async generate(): Promise<void> {
        // Getting reservations' initial state information and initializing analysis data which need it
        let systemReservations: SystemReservations = new SystemReservations();
        systemReservations.init(this.history).then( () => {
            console.log("Init System Reservations"); 
            this.bikesPerStation.setReservations(systemReservations.getReservations());
            this.bikesPerStationCounter++;
            this.rentalAndReturnCalculator.subscribe(this.bikesPerStation);          
            this.calculateRentalsAndReturns();
            
            this.reservationCalculator.setReservations(systemReservations.getReservations());
            this.reservationCounter++;
            this.calculateReservations();
        }); 
        
        // Getting stations' initial state information and initializing data of analysis which need it     
       let systemStations: SystemStations = new SystemStations();
        systemStations.init(this.history).then( () => {
            console.log("Init System stations");
            let reservations: ReservationsPerStation = new ReservationsPerStation(systemStations.getStations());
            this.reservationCalculator.subscribe(reservations);
            this.info.set(ReservationsPerStation.name, reservations);
            this.initReservations(reservations);  // it's async

            let rentalsAndReturns: RentalsAndReturnsPerStation = new RentalsAndReturnsPerStation(systemStations.getStations()); 
            this.rentalAndReturnCalculator.subscribe(rentalsAndReturns);
            this.info.set(RentalsAndReturnsPerStation.name, rentalsAndReturns);  
            this.initRentalsAndReturns(rentalsAndReturns);  // it's async
            
            this.bikesPerStation.init(systemStations.getStations()).then( () => {
                this.bikesPerStationCounter++;
                this.calculateRentalsAndReturns();
            });
        });
        
        // Getting users' initial state information and initializing data of analysis which need it
        let systemUsers: SystemUsers = new SystemUsers(); 
        systemUsers.init(this.history).then( () => {
            console.log("Init System Users");
            let reservations: ReservationsPerUser = new ReservationsPerUser(systemUsers.getUsers());
            this.reservationCalculator.subscribe(reservations);
            this.info.set(ReservationsPerUser.name, reservations);
            this.initReservations(reservations);

            let rentalsAndReturns: RentalsAndReturnsPerUser = new RentalsAndReturnsPerUser(systemUsers.getUsers()); 
            this.rentalAndReturnCalculator.subscribe(rentalsAndReturns);
            this.info.set(RentalsAndReturnsPerUser.name, rentalsAndReturns);  
            this.initRentalsAndReturns(rentalsAndReturns);
        });
        return; 
    }

    private async calculateReservations(): Promise<void> {
        if (this.reservationCounter === this.RESERVATIONS) {
            this.reservationCalculator.calculate().then( () => {
                if (this.csv) {
                    this.calculationCounter++;
                    this.write();
                }
            });
        }
        return;
    }

    private async calculateRentalsAndReturns(): Promise<void> {
        if (this.rentalAndReturnCounter === this.RENTALS_AND_RETURNS && this.bikesPerStationCounter === this.BIKES_PER_STATION) {
            this.rentalAndReturnCalculator.calculate().then( () => {
                let emptyStations: EmptyStationInfo = new EmptyStationInfo(this.bikesPerStation);
                emptyStations.init();
                this.info.set(EmptyStationInfo.name, emptyStations);
                
                if (this.csv) {
                    this.calculationCounter++;
                    this.write();
                }
            });
        }
    }
    
    public static async create(historyPath: string, csvPath?: string, schemaPath?: string): Promise<DataGenerator> {
        console.log('creating data generator:', historyPath);
        let generator: DataGenerator = new DataGenerator(historyPath, csvPath, schemaPath);
        try {
            await generator.init();
            await generator.generate();
        }
        catch(error) {
            throw new Error('Error initializing data generator: '+error);
        }
        return generator;
    }
   
    private async write(): Promise<void> {
        if (this.calculationCounter === this.CALCULATION) {
          let globalValues: SystemGlobalInfo = new SystemGlobalInfo(this.info);
          await globalValues.init(this.historyPath, this.schemaPath);
          globalValues.calculateGlobalData();
          if (this.csvPath !== undefined) {
            let generator: CsvGenerator = new CsvGenerator(this.historyPath, globalValues, this.csvPath, this.schemaPath);
            try {
              await generator.generate(this.info);
          }
          catch(error) {
              throw new Error('Error generating csv file: '+error);
          }
        }
        return;
    }

    public getInfo(): Map<string, SystemInfo> {
		return this.info;
	}
     
}