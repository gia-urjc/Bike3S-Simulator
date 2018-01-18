import { HistoryReader } from '../../../../util';
import { HistoryEntitiesJson } from '../../../../../shared/history';
import { HistoryIterator } from "../../../HistoryIterator";
import { Observer } from '../../ObserverPattern';
import  { Station } from '../../../systemDataTypes/Entities';
import  { TimeEntry, Event } from '../../../systemDataTypes/SystemInternalData';

export class RentalsAndReturnsPerStation implements Observer {
    private stations: Array<Station>;
    private bikeFailedRentalsPerStation: Map<number, number>;
    private bikeSuccessfulRentalsPerStation: Map<number, number>;
    private bikeFailedReturnsPerStation: Map<number, number>;
    private bikeSuccessfulReturnsPerStation: Map<number, number>;
    
    public constructor() {
        this.bikeFailedRentalsPerStation= new Map<number, number>();
        this.bikeSuccessfulRentalsPerStation = new Map<number, number>();
        this.bikeFailedReturnsPerStation = new Map<number, number>();
        this.bikeSuccessfulReturnsPerStation = new Map<number, number>();
    }
    
    public async init(path: string): Promise<boolean> {
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
            return true;
        }
        catch(error) {
            console.log('error getting stations when initializing rentals and returns values:', error);
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
    
    public update(timeEntry: TimeEntry): void {
        let events: Array<Event> = timeEntry.events;
        let stationPosition, routeLastPoint: any;
        
        let key: number;
        let value: number | undefined; 
        
        for (let event of events) {
                
            if (event.name === 'EventUserArrivesAtStationToRentBikeWithReservation' 
                && event.changes.stations.length > 0) {
                if (event.changes.stations.length === 1) {
                    key = event.changes.stations[0].id;
                }
                else {
                    stationPosition = event.changes.users[0].position.new;
                    key = this.getStationId(stationPosition);
                }
    
                value = this.bikeSuccessfulRentalsPerStation.get(key);
                if (value !== undefined) {
                    this.bikeSuccessfulRentalsPerStation.set(key, ++value);
                }
            }
            
            else if (event.name === 'EventUserArrivesAtStationToReturnBikeWithReservation'
                && event.changes.stations.length > 0) {
                if (event.changes.stations.length === 1) {
                    key = event.changes.stations[0].id;
                }
                else {
                    routeLastPoint = event.changes.users[0].route.old.points.length-1; 
                    stationPosition = event.changes.users[0].route.old.points[routeLastPoint];
                    key = this.getStationId(stationPosition);
                }
                
                value = this.bikeSuccessfulReturnsPerStation.get(key);
                if (value !== undefined) {
                    this.bikeSuccessfulReturnsPerStation.set(key, ++value);
                }
            }
                
            else if (event.name === 'EventUserArrivesAtStationToRentBikeWithoutReservation') {
                if(event.changes.users[0] === undefined) {
                    console.log(timeEntry);     
                }
                routeLastPoint = event.changes.users[0].route.old.points.length-1;            
                stationPosition = event.changes.users[0].route.old.points[routeLastPoint];
                key = this.getStationId(stationPosition);
                    
                if (event.changes.stations.length > 0) {   
                    value = this.bikeSuccessfulRentalsPerStation.get(key);
                    if (value !== undefined) {
                        this.bikeSuccessfulRentalsPerStation.set(key, ++value);
                    }
                }
                else {
                    value = this.bikeFailedRentalsPerStation.get(key);
                    if (value !== undefined) {
                        this.bikeFailedRentalsPerStation.set(key, ++value);
                    }
                }
            }
            
            else if (event.name === 'EventUserArrivesAtStationToReturnBikeWithoutReservation') {
                routeLastPoint = event.changes.users[0].route.old.points.length-1; 
                stationPosition = event.changes.users[0].route.old.points[routeLastPoint];
                key = this.getStationId(stationPosition);
                    
                if (event.changes.stations.length > 0) {
                    value = this.bikeSuccessfulReturnsPerStation.get(key);
                    if (value !== undefined) {
                        this.bikeSuccessfulReturnsPerStation.set(key, ++value);
                    }
                }
                else {
                    value = this.bikeFailedReturnsPerStation.get(key);
                    if (value !== undefined) {
                        this.bikeFailedReturnsPerStation.set(key, ++value);
                    }
                }
            }
        }
    }
    
    /* This meth<od is used for events of type bike rental or return without reservation,
     * where involved station may not change its state (user can't rent or return a bike) 
     * and, then, its id isn't registered.  
     */ 
    private getStationId(stationPosition: any): number {
        let stationId: number = -1;
        for(let station of this.stations) {
            if (station.position.latitude === stationPosition.latitude && station.position.longitude === stationPosition.longitude) {
                stationId = station.id;
                break; 
            }
        }

        return stationId;
    }
    
    public getBikeFailedRentalsOfStation(stationId: number): number | undefined {
        return this.bikeFailedRentalsPerStation.get(stationId);
    }
    
    public getBikeSuccessfulRentalsOfStation(stationId: number): number | undefined {
        return this.bikeSuccessfulRentalsPerStation.get(stationId);
    }
    
    public getBikeFailedReturnsOfStation(stationId: number): number | undefined {
        return this.bikeFailedReturnsPerStation.get(stationId);
    }
    
    public getBikeSuccessfulReturnsOfStation(stationId: number): number | undefined {
        return this.bikeSuccessfulReturnsPerStation.get(stationId);
    }

}
