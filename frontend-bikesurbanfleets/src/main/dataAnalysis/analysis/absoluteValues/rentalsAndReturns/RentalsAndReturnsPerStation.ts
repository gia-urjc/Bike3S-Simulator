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
    
    public constructor() {
        this.bikeFailedRentalsPerStation = new Map<number, AbsoluteValue>();
        this.bikeSuccessfulRentalsPerStation = new Map<number, AbsoluteValue>();
        this.bikeFailedReturnsPerStation = new Map<number, AbsoluteValue>();
        this.bikeSuccessfulReturnsPerStation = new Map<number, AbsoluteValue>();
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
        let absoluteValue: AbsoluteValue | undefined; 
        
        for (let event of events) {
                
            if (event.name === 'EventUserArrivesAtStationToRentBikeWithReservation' 
                && event.changes.stations.length > 0) {
                key = this.obtainChanedStation(id(event.changes.stations));
                absoluteValue = this.bikeSuccessfulRentalsPerStation.get(key);
                if (absoluteValue !== undefined) {  // a gotten map value could be undefined
                    absoluteValue.value++;
                }
            }
            
            else if (event.name === 'EventUserArrivesAtStationToReturnBikeWithReservation'
                && event.changes.stations.length > 0) {
                key = this.obtainchangedStationId(event.changes.stations);
                absoluteValue = this.bikeSuccessfulReturnsPerStation.get(key);
                if (absoluteValue !== undefined) {  // a gotten map value can be undefined
                    absoluteValue.value++; 
                }
            }
                
            else if (event.name === 'EventUserArrivesAtStationToRentBikeWithoutReservation') {
            
                if(event.changes.users[0] === undefined) {
                    console.log("There not registered users in an arrival event");     
                }
                if (event.changes.stations > 0) {
                    key = this.obtainChangedStationId(event.changes.stations);
                    if (key !== undefined) {
                        absolluteValue = this.bikeSuccessfulRentalsPerStation.get(key);
                        if (absoluteValue !== undefined) {
                            absoluteValue.value++;
                        }
                    }
                }
                else {
                    key = this.obtainNotChangedStationId(event.changes.users[0]);
                    absoluteValue = this.bikeFailedRentalsPerStation.get(key);
                    if (absoluteValue !== undefined) {
                        absoluteValue.value++;
                    }
                }
            }
            
            else if (event.name === 'EventUserArrivesAtStationToReturnBikeWithoutReservation') {
                if (event.changes.stations.length > 0) {
                    key = this.obtainChanedStationId(event.changes.stations);
                    if (key !== undefined) {
                        absoluteValue = this.bikeSuccessfulReturnsPerStation.get(key);
                        if (absoluteValue !== undefined) {
                            absoluteValue.value++;
                        }
                    }
                }
                else {
                    key = this.obtainNotChangedStationId(event.changes.users[0]);
                    absoluteValue = this.bikeFailedReturnsPerStation.get(key);
                    if (absoluteValue !== undefined) {
                        absoluteValue.value++;
                    }
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
    
    public getBikeFailedRentalsOfStation(stationId: number): A| undefined {
        let value: AbsoluteValue = this.bikeFailedRentalsPerStation.get(stationId);
        return absoluteValue.value;
    }
    
    public getBikeSuccessfulRentalsOfStation(stationId: number): number | undefined {
        let value: AbsoluteValue = this.bikeSuccessfulRentalsPerStation.get(stationId);
        return absoluteValue.value;
    }
    
    public getBikeFailedReturnsOfStation(stationId: number): number | undefined {
        let value: AbsoluteValue = this.bikeFailedReturnsPerStation.get(stationId);
        return absoluteValue.value; 
    }
    
    public getBikeSuccessfulReturnsOfStation(stationId: number): number | undefined {
        let value: AbsoluteValue = this.bikeSuccessfulReturnsPerStation.get(stationId);
        return absoluteValue.value;
    }
  
  public toString(type: string): string {
      let str: any = '';
      switch(type) {
          case "Failed bike returns": {
              this.bikeFailedReturnsPerStation.forEach( (absoluteValue, key) => str += 'Station'+key+absoluteValue.name+':'+absolutevalue.value));
              break;
          }
          case "Failed bike rentals": {
              this.bikeFailedRentalsPerStation.forEach( (absoluteValue, key) => str += 'Station'+key+absoluteValue.name+':'+absolutevalue.value)); 
          }
              case "Successful bike reutrns": {
                  this.bikeSuccessfulReturnsPerStation.forEach( (absoluteValue, key) => str += 'Station'+key+absoluteValue.name+':'+absolutevalue.value));
              break;
          }
          case "Successful bike rentals": {
              this.bikeSuccessfulRentalsPerStation.this.bikeSuccessfulRentalsPerStation.forEach( (value, key) => console.log('Station', key, 'Bike successful rentals', value));
              break;
          }
              
      }
      return src;
    
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
