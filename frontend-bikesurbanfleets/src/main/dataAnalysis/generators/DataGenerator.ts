import { Iterator } from '../analyzers/iterators/Iterator';
import { SystemInfo } from "../analyzers/SystemInfo";
import { RentalsAndReturnsPerStation } from "../analyzers/metrics/rentalsAndReturns/RentalsAndReturnsPerStation";
import { RentalsAndReturnsPerUser } from "../analyzers/metrics/rentalsAndReturns/RentalsAndReturnsPerUser";
import { ReservationsPerStation } from "../analyzers/metrics/reservations/ReservationsPerStation";
import { ReservationsPerUser } from "../analyzers/metrics/reservations/ReservationsPerUser";
import { BikesPerStationAndTime } from "../analyzers/metrics/stations/BikesPerStationAndTime";
import { EmptyStationInfo } from "../analyzers/metrics/stations/EmptyStationInfo";
import { TimeEntryIterator } from "../analyzers/iterators/TimeEntryIterator";
import { SystemStations } from "../systemDataTypes/SystemStations";
import { SystemUsers } from "../systemDataTypes/SystemUsers";
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
    }
    
    private init(): void {
        try {
            this.history = HistoryReader.create(this.historyPath, this.schemaPath);
            let globalValues: any = this.history.getGlobalValues();
            this.totalSimulationTime = globalValues.totalTimeSimulation;
        }
        catch(error) {
            throw new Error('Error reading history file: '+error);
        }
        return;
    } 

    public generate(): void {
        let timeEntryIterator: TimeEntryIterator = new TimeEntryIterator();
        timeEntryIterator.setHistory(this.history);
        this.iterators.set(TimeEntryIterator.name, timeEntryIterator);
   
        //get stations and set up users
        this.systemStations.init(this.history);
        this.systemUsers.init();
        timeEntryIterator.subscribe(this.systemUsers);
    
        //set the reservationsperstations object
        let reservationsPerStation: ReservationsPerStation = new ReservationsPerStation();
        timeEntryIterator.subscribe(reservationsPerStation);          
        this.info.set(ReservationsPerStation.name, reservationsPerStation);
        reservationsPerStation.init();


        //set the rentalsandreturnsperstation object
        let rentalsAndReturnsPerStation: RentalsAndReturnsPerStation = new RentalsAndReturnsPerStation(); 
        timeEntryIterator.subscribe(rentalsAndReturnsPerStation);
        this.info.set(RentalsAndReturnsPerStation.name, rentalsAndReturnsPerStation);  
        rentalsAndReturnsPerStation.init();
        
        //set up the bikes per station and time object
        timeEntryIterator.subscribe(this.bikesPerStation);          
        this.bikesPerStation.init(this.systemStations.getStations());
                
        // Getting users' initial state information and initializing data of analysis which need it
        let reservationsPerUser: ReservationsPerUser = new ReservationsPerUser();
        timeEntryIterator.subscribe(reservationsPerUser);
        this.info.set(ReservationsPerUser.name, reservationsPerUser);
        reservationsPerUser.init();

        let rentalsAndReturnsPerUser: RentalsAndReturnsPerUser = new RentalsAndReturnsPerUser(); 
        timeEntryIterator.subscribe(rentalsAndReturnsPerUser);
        this.info.set(RentalsAndReturnsPerUser.name, rentalsAndReturnsPerUser);  
        rentalsAndReturnsPerUser.init();
        
        timeEntryIterator.subscribe(this.userInstants);

        this.calculateEventValues();
        this.calculateGlobalInfo();
    }

    private calculateEventValues(): void {
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
            throw new Error('Error calculating Event valuess. \n'
            + 'Iterator with name: ' + TimeEntryIterator.name + ' is undefined');
        }
    }
    
    private calculateGlobalInfo(): void {
        this.globalInfo = new SystemGlobalInfo();
        let reservations: SystemInfo | undefined = this.info.get(ReservationsPerStation.name);
        let rentalsAndReturns: SystemInfo | undefined = this.info.get(RentalsAndReturnsPerStation.name);
            
        if(reservations && rentalsAndReturns) {
            this.globalInfo.calculateGlobalData(reservations, rentalsAndReturns, this.systemUsers.getUsers().length);
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
                            console.log('before writing');

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