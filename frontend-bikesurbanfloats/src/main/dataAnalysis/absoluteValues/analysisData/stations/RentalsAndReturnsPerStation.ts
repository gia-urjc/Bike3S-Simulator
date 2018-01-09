import { HistoryReader } from '../../../../util';
import { HistoryEntitiesJson } from '../../../../../shared/history';
import { Observer } from '../../ObserverPattern';
import  { Station } from '../../../systemDataTypes/Entities';
import  { TimeEntry } from '../../../systemDataTypes/SystemInternalData';

export class RentalsAndReturnsPerStation implements Observer {
    private stations: Array<Station>;
    private bikeFailedRentalsPerStation: Map<number, number>;
    private bikeSuccessfulRentalsPerStation: Map<number, number>;
    private bikeFailedReturnsPerStation: Map<number, number>;
    private bikeSuccessfulReturnsPerStation: Map<number, number>;
    
    private constructor() {
        this.bikeFailedRentalsPerStation= new Map<number, number>();
        this.bikeSuccessfulRentalsPerStation = new Map<number, number>();
        this.bikeFailedReturnsPerStation = new Map<number, number>();
        this.bikeSuccessfulReturnsPerStation = new Map<number, number>();
    }
    
    private async init(path:  string): Promise<void> {
        let history: HistoryReader = await HistoryReader.create(path);
        let entities: HistoryEntitiesJson = await history.getEntities("stations");
        this.stations = <Station[]> entities.instances;
                
        for(let station of this.stations) {
            this.bikeFailedRentalsPerStation.set(station.id, 0);
            this.bikeSuccessfulRentalsPerStation.set(station.id, 0);
            this.bikeFailedReturnsPerStation.set(station.id, 0);            
            this.bikeSuccessfulReturnsPerStation.set(station.id, 0);            
        }
 
    }
    
    public static async create(path: string): Promise<RentalsAndReturnsPerStation> {
        let stationValues = new RentalsAndReturnsPerStation();
        await stationValues.init(path);
        return stationValues;
    }
    
    public update(timeEntry: TimeEntry): void {
        
    }

}
