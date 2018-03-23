import  { Station, User } from '../../../systemDataTypes/Entities';
import  { TimeEntry, Event } from '../../../systemDataTypes/SystemInternalData';
import { Observer } from "../../ObserverPattern";
import { Data } from "../Data";
import { SystemInfo } from "../SystemInfo";
import { RentalAndReturnData } from './RentalAndReturnData';

export class RentalsAndReturnsPerStation implements SystemInfo, Observer {
    basicData: Array<Station>;
    data: Data;
    
    public constructor(stations: Array<Station>) {
        this.basicData = stations;
        this.data = new RentalAndReturnData();
    }
    
<<<<<<< HEAD
    public async init() {
        try {
            await this.data.initData(this.basicData);
=======
    public async init(path: string, schemaPath?: string | null): Promise<void> {
        try {
            let history: HistoryReader = await HistoryReader.create(path, schemaPath);
            let entities: HistoryEntitiesJson = await history.getEntities("stations");
            this.stations = <Station[]> entities.instances;
                
            for(let station of this.stations) {
                this.bikeFailedRentalsPerStation.set(station.id, 0);
                this.bikeSuccessfulRentalsPerStation.set(station.id, 0);
                this.bikeFailedReturnsPerStation.set(station.id, 0);            
                this.bikeSuccessfulReturnsPerStation.set(station.id, 0);            
            }
>>>>>>> d86b148f5d966d645a819dde4afc777d22832467
        }
        catch(error) {
            throw new Error('Error initializing data: '+error);
        }
        return;
    }
    
<<<<<<< HEAD
    public static async create(stations: Array<Station>) {
        let stationValues = new RentalsAndReturnsPerStation(stations);
        try {
            await stationValues.init();
=======
    public static async create(path: string, schemaPath?: string | null): Promise<RentalsAndReturnsPerStation> {
        let stationValues = new RentalsAndReturnsPerStation();
        try {
            await stationValues.init(path, schemaPath);
>>>>>>> d86b148f5d966d645a819dde4afc777d22832467
        }
        catch(error) {
            throw new Error('Error creating requested data: '+error);
        }
        return stationValues;
    }
  
    public update(timeEntry: TimeEntry): void {
        let events: Array<Event> = timeEntry.events;
        let key: number | undefined;
        let eventStations: Array<Station>;
        
        for (let event of events) {
            eventStations = event.changes.stations;
            
            switch(event.name) {
                case 'EventUserArrivesAtStationToRentBikeWithReservation': { 
                    key = this.obtainChangedStationId(eventStations);
                    if (key !== undefined) {  // it's sure key isn't undefined because the user has a bike reservation
                        this.data.increaseSuccessfulRentals(key);
                    }
                    break;
                }
            
                case 'EventUserArrivesAtStationToReturnBikeWithReservation': {
                    key = this.obtainChangedStationId(eventStations);
                    if (key !== undefined) {  // it's sure key isn't undefined because the user has a bike reservation
                        this.data.increaseSuccessfulReturns(key);
                    }
                    break;
                }
                
                case 'EventUserArrivesAtStationToRentBikeWithoutReservation': {
                    if (eventStations.length > 0) {
                        // If only stations with reservations have been recorded, key'll be undefined 
                        key = this.obtainChangedStationId(eventStations);
                        // If key is undefined, successful rentals won't be increased
                        if (key  !== undefined) {
                            this.data.increaseSuccessfulRentals(key);
                        }
                    }
                    
                    // If there're not registered stations, it means rental hasn't been possible
                    else {
                        key = this.obtainNotChangedStationId(event.changes.users[0]);
                        this.data.increaseFailedRentals(key);
                    }
                    break;
                }
            
                case 'EventUserArrivesAtStationToReturnBikeWithoutReservation': {
                    if (eventStations.length > 0) {
                        // If only stations with reservations have been recorded, key'll be undefined
                        key = this.obtainChangedStationId(eventStations);
                        // If key is undefined, successful returns won't be increased
                        if (key !== undefined) {
                            this.data.increaseSuccessfulReturns(key);
                        }
                    }
                    
                    // If there're not registered stations, it means rental hasn't been possible
                    else {
                        key = this.obtainNotChangedStationId(event.changes.users[0]);
                        this.data.increaseFailedReturns(key);
                    }
                    break;
                }
            }
        }
    }
    
    /**
     * It finds out the station id from the last point of the route travelled by a user,
     * looking for which station is at that point.       
     */
    private obtainNotChangedStationId(user: User): number {
        let lastPos: number = user.route.old.points.length-1;
        let stationPosition: any = user.route.old.points[lastPos];
         
        let stationId: number = -1;
        for(let station of this.basicData) {
            if (station.position.latitude === stationPosition.latitude && station.position.longitude === stationPosition.longitude) {
                stationId = station.id;
                break; 
            }
        }
        return stationId;
    }
    
    /**
     * It finds out the station id lokking for a change registered on the station 
     * bikes; if only changes in reservations have been registered, it returns undefined    
     */
    private obtainChangedStationId(stations: Array<Station>): number | undefined {
        for (let station of stations) {
            if (station.bikes !== undefined) {
                return station.id;
            }
        }
        return undefined;
    }
    
    public getData(): Data {
        return this.data;
    }

}
