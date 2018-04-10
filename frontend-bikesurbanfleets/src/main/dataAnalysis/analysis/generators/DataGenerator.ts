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
    private systemStations: SystemStations;
    private systemUsers: SystemUsers;
    private systemReservations: SystemReservations;
    
    private info: Map<string, SystemInfo>;  // it contains all the results of the data analysis
    private globalInfo: SystemGlobalInfo; 
    private bikesPerStation: BikesPerStation;
    private reservationCalculator: ReservationCalculator;
    private rentalAndReturnCalculator: RentalAndReturnCalculator;

    private constructor(historyPath: string, csvPath?: string, schemaPath?: string) {
        this.csv = csvPath == undefined ? false: true;
        this.historyPath = historyPath;
        this.schemaPath = schemaPath;
        this.csvPath = csvPath;
        this.reservationCounter = 0;
        this.rentalAndReturnCounter = 0;
        this.calculationCounter = 0;
        this.bikesPerStationCounter = 0;
        this.info = new Map();
        this.bikesPerStation = new BikesPerStation();
        this.rentalAndReturnCalculator = new RentalAndReturnCalculator();
        this.reservationCalculator = new ReservationCalculator();
        this.systemStations = new SystemStations();
        this.systemUsers = new SystemUsers();
        this.systemReservations = new SystemReservations();
    }
    
    private async init(): Promise<void> {
        try {
            this.history = await HistoryReader.create(this.historyPath, this.schemaPath);
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
        this.systemReservations.init(this.history).then( () => {
            this.bikesPerStation.setReservations(this.systemReservations.getReservations());
            this.bikesPerStationCounter++;
            this.rentalAndReturnCalculator.subscribe(this.bikesPerStation);          
            this.calculateRentalsAndReturns();
            
            this.reservationCalculator.setReservations(this.systemReservations.getReservations());
            this.reservationCounter++;
            this.calculateReservations();
        }); 
        
        // Getting stations' initial state information and initializing data of analysis which need it     
        this.systemStations.init(this.history).then( () => {
            let reservations: ReservationsPerStation = new ReservationsPerStation(this.systemStations.getStations());
            this.reservationCalculator.subscribe(reservations);
            this.info.set(ReservationsPerStation.name, reservations);
            this.initReservations(reservations);  // it's async

            let rentalsAndReturns: RentalsAndReturnsPerStation = new RentalsAndReturnsPerStation(this.systemStations.getStations()); 
            this.rentalAndReturnCalculator.subscribe(rentalsAndReturns);
            this.info.set(RentalsAndReturnsPerStation.name, rentalsAndReturns);  
            this.initRentalsAndReturns(rentalsAndReturns);  // it's async
            
            this.bikesPerStation.init(this.systemStations.getStations()).then( () => {
                this.bikesPerStationCounter++;
                this.calculateRentalsAndReturns();
            });
        });
        
        // Getting users' initial state information and initializing data of analysis which need it
        this.systemUsers.init(this.history).then( () => {
            let reservations: ReservationsPerUser = new ReservationsPerUser(this.systemUsers.getUsers());
            this.reservationCalculator.subscribe(reservations);
            this.info.set(ReservationsPerUser.name, reservations);
            this.initReservations(reservations);

            let rentalsAndReturns: RentalsAndReturnsPerUser = new RentalsAndReturnsPerUser(this.systemUsers.getUsers()); 
            this.rentalAndReturnCalculator.subscribe(rentalsAndReturns);
            this.info.set(RentalsAndReturnsPerUser.name, rentalsAndReturns);  
            this.initRentalsAndReturns(rentalsAndReturns);
        });
        return; 
    }

    private async calculateReservations(): Promise<void> {
        if (this.reservationCounter === this.RESERVATIONS) {
            this.reservationCalculator.calculate().then( () => {
            this.calculationCounter++;
                this.calculateGlobalInfo();
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
                
                this.calculationCounter++;
                this.calculateGlobalInfo();
            });
        }
    }
    
    private async calculateGlobalInfo(): Promise<void> {
        if (this.calculationCounter === this.CALCULATION) {
            this.calculationCounter++;
            this.globalInfo = new SystemGlobalInfo(this.systemUsers.getUsers());
            let reservations: SystemInfo | undefined = this.info.get(ReservationsPerStation.name);
            let rentalsAndReturns: SystemInfo | undefined = this.info.get(RentalsAndReturnsPerStation.name);
            if(reservations && rentalsAndReturns) {
                this.globalInfo.calculateGlobalData(reservations, rentalsAndReturns);
                if (this.csv) {
                    this.write();
                }
            }
        }
        return;
    }
    
    public static async create(historyPath: string, csvPath?: string, schemaPath?: string): Promise<DataGenerator> {
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
        if (this.calculationCounter === this.CALCULATION + 1 && this.csvPath !== undefined) {
            let generator: CsvGenerator = new CsvGenerator(this.csvPath);
            try {
              await generator.generate(this.info, this.globalInfo, this.systemStations.getStations(), this.systemUsers.getUsers());
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