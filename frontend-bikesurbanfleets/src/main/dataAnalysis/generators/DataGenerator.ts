import { Iterator } from '../analyzers/iterators/Iterator';
import { SystemInfo } from "../analyzers/SystemInfo";
import { RentalsAndReturnsPerStation } from "../analyzers/metrics/rentalsAndReturns/RentalsAndReturnsPerStation";
import { RentalsAndReturnsPerUser } from "../analyzers/metrics/rentalsAndReturns/RentalsAndReturnsPerUser";
import { ReservationsPerStation } from "../analyzers/metrics/reservations/ReservationsPerStation";
import { ReservationsPerUser } from "../analyzers/metrics/reservations/ReservationsPerUser";
import { BikesPerStationAndTime } from "../analyzers/metrics/stations/BikesPerStationAndTime";
import { EmptyStationInfo } from "../analyzers/metrics/stations/EmptyStationInfo";
import { ReservationIterator } from "../analyzers/iterators/ReservationIterator";
import { TimeEntryIterator } from "../analyzers/iterators/TimeEntryIterator";
import { SystemReservations } from "../historyEntities/SystemReservations";
import { SystemStations } from "../historyEntities/SystemStations";
import { SystemUsers } from "../historyEntities/SystemUsers";
import { CsvGenerator } from "./CsvGenerator";
import { SystemGlobalInfo } from '../analyzers/metrics/SystemGlobalInfo';
import { StationBalanceQuality } from '../analyzers/metrics/stations/StationBalanceQuality';
import { UserFactInstantInfo } from '../analyzers/metrics/users/UserFactInstantInfo';
import { UserTimeAtSystem } from '../analyzers/metrics/users/UserTimeAtSystem';
import { HistoryReader } from '../HistoryReader';

export class DataGenerator {

    private csv: boolean;  // it indicates if a csv file must be generated    
    private historyPath: string;  
    private schemaPath?: string;
    private csvPath?: string;
    
    private history: HistoryReader; 
    private systemStations: SystemStations;
    private systemUsers: SystemUsers;
    private systemReservations: SystemReservations;
    private totalSimulationTime: number;
    
    private info: Map<string, SystemInfo>;  // it contains all the results of the data analysis
    private globalInfo: SystemGlobalInfo; 
    private bikesPerStation: BikesPerStationAndTime;
    private userInstants: UserFactInstantInfo;
    private iterators: Map<string, Iterator>;

    public static create(historyPath: string, csvPath?: string, schemaPath?: string): DataGenerator {
        let generator: DataGenerator = new DataGenerator(historyPath, csvPath, schemaPath);
        try {
            generator.init();
            generator.generate();
        }
        catch(error) {
            throw new Error('Error initializing data generator: '+error);
        }
        return generator;
    }
   
    private constructor(historyPath: string, csvPath?: string, schemaPath?: string) {
        this.csv = csvPath === undefined ? false: true;
        this.historyPath = historyPath;
        this.schemaPath = schemaPath;
        this.csvPath = csvPath;
        this.info = new Map();
        this.bikesPerStation = new BikesPerStationAndTime();
        this.userInstants = new UserFactInstantInfo();
        this.iterators = new Map();
        this.systemStations = new SystemStations();
        this.systemUsers = new SystemUsers();
        this.systemReservations = new SystemReservations();
    }
    
    private init(): void {
        try {
            this.history = HistoryReader.create(this.historyPath, this.schemaPath);
            let globalValues: any = this.history.getGlobalValues();
            this.totalSimulationTime = globalValues.totalTimeSimulation;
            console.log('total time: '+this.totalSimulationTime);
        }
        catch(error) {
            throw new Error('Error reading history file: '+error);
        }
        return;
    } 

    private initReservations(reservations: SystemInfo): void {
        reservations.init();
    }

    private initRentalsAndReturns(rentalsAndReturns: SystemInfo): void {
        rentalsAndReturns.init();
    }

    public generate(): void {
        let reservationIterator: ReservationIterator = new ReservationIterator();  
        let timeEntryIterator: TimeEntryIterator = new TimeEntryIterator();
        timeEntryIterator.setHistory(this.history);
        this.iterators.set(ReservationIterator.name, reservationIterator);
        this.iterators.set(TimeEntryIterator.name, timeEntryIterator);
        
        // Getting reservations' initial state information and initializing analysis data which need it
        this.systemReservations.init(this.history);
        this.bikesPerStation.setReservations(this.systemReservations.getReservations());
        timeEntryIterator.subscribe(this.bikesPerStation);          
        reservationIterator.setReservations(this.systemReservations.getReservations());
        
        // Getting stations' initial state information and initializing data of analysis which need it     
        this.systemStations.init(this.history);
        let reservationsPerStation: ReservationsPerStation = new ReservationsPerStation(this.systemStations.getStations());
        reservationIterator.subscribe(reservationsPerStation);
        this.info.set(ReservationsPerStation.name, reservationsPerStation);
        this.initReservations(reservationsPerStation);  // it's async

        let rentalsAndReturnsPerStation: RentalsAndReturnsPerStation = new RentalsAndReturnsPerStation(this.systemStations.getStations()); 
        timeEntryIterator.subscribe(rentalsAndReturnsPerStation);
        this.info.set(RentalsAndReturnsPerStation.name, rentalsAndReturnsPerStation);  
        this.initRentalsAndReturns(rentalsAndReturnsPerStation);  // it's async
        
        this.bikesPerStation.init(this.systemStations.getStations());
        
        // Getting users' initial state information and initializing data of analysis which need it
        this.systemUsers.init(this.history);
        let reservationsPerUser: ReservationsPerUser = new ReservationsPerUser(this.systemUsers.getUsers());
        reservationIterator.subscribe(reservationsPerUser);
        this.info.set(ReservationsPerUser.name, reservationsPerUser);
        this.initReservations(reservationsPerUser);

        let rentalsAndReturnsPerUser: RentalsAndReturnsPerUser = new RentalsAndReturnsPerUser(this.systemUsers.getUsers()); 
        timeEntryIterator.subscribe(rentalsAndReturnsPerUser);
        this.info.set(RentalsAndReturnsPerUser.name, rentalsAndReturnsPerUser);  
        this.initRentalsAndReturns(rentalsAndReturnsPerUser);
        
        timeEntryIterator.subscribe(this.userInstants);
        this.userInstants.init(this.systemUsers.getUsers());

        this.calculateReservations();
        this.calculateRentalsAndReturns();
        this.calculateGlobalInfo();
    }

    private calculateReservations(): void {
        let iterator: Iterator | undefined = this.iterators.get(ReservationIterator.name);
        if(iterator) {
            iterator.iterate();
            return;
        }
        else {
            throw new Error('Error calculating Reservations. \n'
            + 'Iterator with name: ' + ReservationIterator.name + ' is undefined');
        }
    }

    private calculateRentalsAndReturns(): void {
        let iterator: Iterator | undefined = this.iterators.get(TimeEntryIterator.name);
        if(iterator) {
            iterator.iterate();
            let emptyStations: EmptyStationInfo = new EmptyStationInfo(this.bikesPerStation, this.totalSimulationTime);
            this.info.set(EmptyStationInfo.name, emptyStations);
            emptyStations.init();
                                
            let bikesBalance: StationBalanceQuality = new StationBalanceQuality(this.bikesPerStation, this.totalSimulationTime);
            bikesBalance.setStations(this.systemStations.getStations());
            this.info.set(StationBalanceQuality.name, bikesBalance);
            bikesBalance.init();
            
            let userTimeIntervals: UserTimeAtSystem = new UserTimeAtSystem(this.userInstants);
            this.info.set(UserTimeAtSystem.name, userTimeIntervals);
            userTimeIntervals.init();
            return;
        }
        else {
            throw new Error('Error calculating Rentals and returns. \n'
            + 'Iterator with name: ' + TimeEntryIterator.name + ' is undefined');
        }
    }
    
    private calculateGlobalInfo(): void {
        this.globalInfo = new SystemGlobalInfo(this.systemUsers.getUsers());
        let reservations: SystemInfo | undefined = this.info.get(ReservationsPerStation.name);
        let rentalsAndReturns: SystemInfo | undefined = this.info.get(RentalsAndReturnsPerStation.name);
        
        if(reservations && rentalsAndReturns) {
            this.globalInfo.calculateGlobalData(reservations, rentalsAndReturns);
            if (this.csv) {
                this.write();
            }
        }
        return;
    }
    
    private write(): void {
        if (this.csvPath !== undefined) {
            let generator: CsvGenerator = new CsvGenerator(this.csvPath);
            try {
                generator.generate(this.info, this.globalInfo, this.bikesPerStation, this.systemStations.getStations(), this.systemUsers.getUsers());
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