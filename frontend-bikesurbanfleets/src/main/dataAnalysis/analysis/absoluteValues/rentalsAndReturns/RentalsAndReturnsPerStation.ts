import  { Station, User } from '../../../systemDataTypes/Entities';
import  { TimeEntry, Event } from '../../../systemDataTypes/SystemInternalData';
import { Observer } from "../../ObserverPattern";
import { Data } from "../Data";
import { SystemInfo } from "../SystemInfo";
import { RentalAndReturnData } from './RentalAndReturnData';

export class RentalsAndReturnsPerStation implements SystemInfo, Observer {
    basicData: Array<Station>;
    data: Data;

    public static async create(stations: Array<Station>) {
        let stationValues = new RentalsAndReturnsPerStation(stations);
        try {
            await stationValues.init();
        }
        catch(error) {
            throw new Error('Error creating requested data: '+error);
        }
        return stationValues;
    }


    public constructor(stations: Array<Station>) {
        this.basicData = stations;
        this.data = new RentalAndReturnData();
    }

    public async init() {
        try {
           await this.data.initData(this.basicData);
        }
        catch(error) {
            throw new Error('Error initializing data: '+error);
        }
        return;
    }

    public update(timeEntry: TimeEntry): void {
        let events: Array<Event> = timeEntry.events;
        let key: number | undefined;
        let eventStations: Array<Station>;
        console.log('Rentals And returns per Station');
        
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
                    console.log(eventStations);
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
        let stationPosition: any;
        if (user.route !== undefined) {
            let lastPos: number = user.route.old.points.length-1;
            stationPosition = user.route.old.points[lastPos];
        } 
        else {
            stationPosition = user.position.new;
        }
        
         
        let stationId = -1;
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
