import { HistoryReader } from '../../../../util';
import { HistoryEntitiesJson } from '../../../../../shared/history';
import { Observer } from '../../ObserverPattern';
import  { Station } from '../../../systemDataTypes/Entities';
import  { TimeEntry, Event } from '../../../systemDataTypes/SystemInternalData';

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
        try {
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
        catch(error) {
            console.log('error initializing station values:', error);
        }
        return;
    }
    
    public static async create(path: string): Promise<RentalsAndReturnsPerStation> {
        let stationValues = new RentalsAndReturnsPerStation();
        try {
            await stationValues.init(path);
            return stationValues;
        }
        catch(error) {
            console.log('error creating station values:', error);
        }
    }
    
    public update(timeEntry: TimeEntry): Promise<void> {
        let name: string;
        let event: Event;
        
        let key: number;
        let value: number | undefined; 
        
        name = 'EventUserArrivesAtStationToRentBikeWithReservation';
        event = HistoryIterator.getEventByName(timeEntry, name);
        if (event !== undefined) {
            key = event.changes.stations[0].id;
            value = this.bikeSuccessfulRentalsPerStation.get(key);
            this.bikeSuccessfulRentalsPerStation.set(key, ++value);
        }
        
        name = 'EventUserArrivesAtStationToReturnBikeWithReservation';
        event = HistoryIterator.getEventByName(timeEntry, name);
        if (event !== undefined && event.changes.stations.length !== 0) {
            key = event.changes.stations[0].id;
            value = this.bikeSuccessfulReturnsPerStation.get(key);
            this.bikeSuccessfulReturnsPerStation.set(key, ++value);
        }
        
        name = 'EventUserArrivesAtStationToRentBikeWithoutReservation';
        event = HistoryIterator.getEventByName(timeEntry, name);
        if (event !== undefined && event.changes.stations.length !== 0) {
            key = event.changes.stations[0].id;
            value = this.bikeSuccessfulRentalsPerStation.get(key);
            this.bikeSuccessfulRentalsPerStation.set(key, ++value);
        }
        else {
            value = this.bikeFailedRentalsPerStation.get(key);
            this.bikeFailedRentalsPerStation.set(key, ++value);
        }
        
        name = 'EventUserArrivesAtStationToReturnBikeWithoutReservation';
        event = HistoryIterator.getEventByName(timeEntry, name);
        if (event !== undefined && event.changes.stations.length !== 0) {
            key = event.changes.stations[0].id;
            value = this.bikeSuccessfulReturnsPerStation.get(key);
            this.bikeSuccessfulReturnsPerStation.set(key, ++value);
        }
        else {
            value = this.bikeFailedReturnsPerStation.get(key);
            this.bikeFailedReturnsPerStation.set(key, ++value);
        }
    }

}
