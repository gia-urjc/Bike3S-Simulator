import { HistoryReader } from '../../../../util';
import { HistoryEntitiesJson } from '../../../../../shared/history';
import { HistoryIterator } from "../../../HistoryIterator";
import  { Station, User } from '../../../systemDataTypes/Entities';
import  { TimeEntry, Event } from '../../../systemDataTypes/SystemInternalData';
import { AbsoluteValue } from '../AbsoluteValue';
import { Data } from '../Data';

export class RentalsAndReturnsPerStation implements Data {
    private stations: Array<Station>;
    private bikeFailedRentalsPerStation: Map<number, AbsoluteValue>;
    private bikeSuccessfulRentalsPerStation: Map<number, AbsoluteValue>;
    private bikeFailedReturnsPerStation: Map<number, AbsoluteValue>;
    private bikeSuccessfulReturnsPerStation: Map<number, AbsoluteValue>;
    private factType: string;
    private entityType: string;
    
    public constructor() {
        this.factType = "RENTAL_AND_RETURN";
        this.entityType = "STATION"; 
        this.bikeFailedRentalsPerStation = new Map<number, AbsoluteValue>();
        this.bikeSuccessfulRentalsPerStation = new Map<number, AbsoluteValue>();
        this.bikeFailedReturnsPerStation = new Map<number, AbsoluteValue>();
        this.bikeSuccessfulReturnsPerStation = new Map<number, AbsoluteValue>();
    }
  
  public getFactType(): string {
    return this.factType;
  }
  
  public getEntityType(): string{
    return this.entityType;
  }
    
    public async init(path: string): Promise<void> {
        try {
            let history: HistoryReader = await HistoryReader.create(path);
            let entities: HistoryEntitiesJson = await history.getEntities("stations");
            this.stations = <Station[]> entities.instances;
                
            for(let station of this.stations) {
                this.bikeFailedRentalsPerStation.set(station.id, { name: "Failed bike rentals", value: 0 });
                this.bikeSuccessfulRentalsPerStation.set(station.id, { name: "Successful bike rentals", value: 0 });
                this.bikeFailedReturnsPerStation.set(station.id, { name: "Failed bike returns", value: 0 });            
                this.bikeSuccessfulReturnsPerStation.set(station.id, { name: "Successful bike returns", value: 0 });            
            }
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
        }
        catch(error) {
            console.log('error creating station values:', error);
        }
        return stationValues;
    }
  
    public update(timeEntry: TimeEntry): void {
        let events: Array<Event> = timeEntry.events;
            
        let key: number | undefined;
        
        for (let event of events) {
            switch(event.name) {
                case 'EventUserArrivesAtStationToRentBikeWithReservation': { 
                    key = this.obtainChangedStationId(event.changes.stations);
                    this.increaseValue(this.bikeSuccessfulRentalsPerStation, key);
                    break;
                }
            
                case 'EventUserArrivesAtStationToReturnBikeWithReservation': {
                    key = this.obtainChangedStationId(event.changes.stations);
                    this.increaseValue(this.bikeSuccessfulReturnsPerStation, key);
                    break;
                }
                
                case 'EventUserArrivesAtStationToRentBikeWithoutReservation': {
                    if (event.changes.stations > 0) {
                        key = this.obtainChangedStationId(event.changes.stations);
                        this.increaseValue(this.bikeSuccessfulRentalsPerStation, key);
                    }
                    else {
                        key = this.obtainNotChangedStationId(event.changes.users[0]);
                        this.increaseValue(this.bikeFailedRentalsPerStation, key);
                    }
                    break;
                }
            
                case 'EventUserArrivesAtStationToReturnBikeWithoutReservation': {
                    if (event.changes.stations.length > 0) {
                        key = this.obtainChangedStationId(event.changes.stations);
                        this.increaseValue(this.bikeSuccessfulReturnsPerStation, key);
                    }
                    else {
                        key = this.obtainNotChangedStationId(event.changes.users[0]);
                        this.increaseValue(this.bikeFailedReturnsPerStation, key);
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
        for(let station of this.stations) {
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
    
    public getBikeFailedRentalsOfStation(stationId: number): number | undefined {
        let absoluteValue: AbsoluteValue = this.bikeFailedRentalsPerStation.get(stationId);
        return absoluteValue.value;
    }
    
    public getBikeSuccessfulRentalsOfStation(stationId: number): number | undefined {
        let absoluteValue: AbsoluteValue = this.bikeSuccessfulRentalsPerStation.get(stationId);
        return absoluteValue.value;
    }
    
    public getBikeFailedReturnsOfStation(stationId: number): number | undefined {
        let absoluteValue: AbsoluteValue = this.bikeFailedReturnsPerStation.get(stationId);
        return absoluteValue.value; 
    }
    
    public getBikeSuccessfulReturnsOfStation(stationId: number): number | undefined {
        let absoluteValue: AbsoluteValue = this.bikeSuccessfulReturnsPerStation.get(stationId);
        return absoluteValue.value;
    }
  
  public getBikeSuccessfulRentals(): Map<number, AbsoluteValue> {
    return this.bikeSuccessfulRentalsPerStation;
  }
  
  public getBikeSuccessfulReturns(): Map<number, AbsoluteValue> {
    return this.bikeSuccessfulReturnsPerStation;
  }
  
  public getBikeFailedRentals(): Map<number, AbsoluteValue> {
    return this.bikeFailedRentalsPerStation;
  }
  
  public getBikeFailedReturns(): Map<number, AbsoluteValue> {
    return this.bikeFailedReturnsPerStation;
  }




}
