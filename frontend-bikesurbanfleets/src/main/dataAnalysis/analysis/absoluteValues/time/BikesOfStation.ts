import { HistoryReader } from '../../../../../../util';
import { HistoryEntitiesJson } from '../../../../../../../shared/history';
import { HistoryIterator } from "../../../../../HistoryIterator";
import { Observer } from '../../../../ObserverPattern';
import  { Station } from '../../../../../systemDataTypes/Entities';
import  { TimeEntry, Event } from '../../../../../systemDataTypes/SystemInternalData';


interface BikesPerTime {
  time: number;
  availableBikes: number;
}

class StationWithBikesPerTime {
  private id: number; 
  private bikesPerTime: Array<BikesPerTime>;
 
  public constructor(id: number, bikes: number) {
    this.id = id;
    this.bikesPerTime = new Array();
    let value: BikesPerTime = {time: 0, availableBikes: bikes};
    this.bikesPerTime.push(value);
  }
  
  public getBikesPerTime(index: number): BikesPerTime {
    return this.bikesPerTime[index];
  }
  
  public addBike(time: number): void {
    let lastPos: number = this.bikesPerTime.length-1;
    let bikes: number = this.bikesPerTime[lastPos].availableBikes++; 
    let value: BikesPerTime = {time: time, availableBikes: bikes};
    this.bikesPerTime.push(value);
  }
  
  public substractBike(time: number): void {
    let lastPos: number = this.bikesPerTime.length-1;
    let bikes: number = this.bikesPerTime[lastPos].availableBikes--; 
    let value: BikesPerTime = {time: time, availableBikes: bikes};
    this.bikesPerTime.push(value);
  }
}

export class BikesOfStation implements Observer {
  private stations: Map<number, Array<StationWithBikesPerTime>>;
  private reservations: Array<Reservation>; 
  
  public async init(path: string): Promise<void> {
      try {
          let history: HistoryReader = await HistoryReader.create(path);
          let entities: HistoryEntitiesJson = await history.getEntities('stations');
          let systemStations = <Station[]> entities.instances;
          entities = await history.getEntities('reservations');
          this.reservations = <Reservation[]> entities.instances;
        
          for(let station of systemStations) {
            let value: StationWithBikesPerTime = {time: 0, availableBikes: numberOfBikes(station)};
            this.stations.set(station.id, value);
          }
      }
      catch(error) {
          console.log('error getting stations:', error);
      }
      return;
  }
  
  public update(timeEntry: TimeEntry): void {
    let instant: number = timeEntry.time;
    let events: Array<Event> = timeEntry.events;
    let stations: Array<Station>; 
    let last, reservationId, bikes: number;
    let reservation: Reservation;
    let bikesPerTimeList: Array<BikesPerTime> | undefined; 
    let value: BikesPerTime; 
    
    for(let event of events) {
      stations = event.changes.stations;
    
      switch(event.name) {
        case "EventUserAppears": {
          if (stations != undefined) {
            for(let station of stations) {
              last = station.reservations.id.length-1;
              reservationId = station.reservations.id[last];
              reservation = getReservation(reservationId);
              if (reservation.state === "ACTIVE") {
                bikesPerTimeList = this.stations.get(station.id);
                if (bikesPerTimeList !== undefined) {
                  last = bikesPerTimeList.length-1;
                  bikes = --bikesPerTimeList[last].availableBikes; 
                  value = {time: instant, availableBikes: bikes});
                  bikesPerTimeList.push(value);
                }
              }
            }
          }
          break;
        }
          
        case "EventUserArrivesAtStationToRentBikeWithoutReservation": {
          break;
        }
          
        case "EventBikeReservationTimeout": {
          break;
        }
          
        case "EventUserArrivesAtStationToReturnBikeWithReservation": {
          break;
        }
          
        case "EventUserArrivesAtStationToReturnBikeWithoutReservatioon": {
          break;
        }
    }
  }
    
  private numberOfBikes(station: Station): number {
    let counter: number = 0;
    station.bikes.id.forEach(bike => if (bike != null) counter++);
    return counter;
  }
  
  private getReservation(id: number): Reservation {
    for(let reservation of this.reservations) {
      if (reservation.id === id) {
        return reservation;
      }
    }
  }

}
