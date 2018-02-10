import { HistoryReader } from '../../../../../../util';
import { HistoryEntitiesJson } from '../../../../../../../shared/history';
import { Observer } from '../../../../ObserverPattern';
import { Station, Reservation } from '../../../../../systemDataTypes/Entities';
import { TimeEntry, Event } from '../../../../../systemDataTypes/SystemInternalData';

interface BikesPerTime {
  time: number;
  availableBikes: number;
}

export class StationBikesPerTimeList {
    private bikesPerTimeList: Array<BikesPerTime>;
 
  public constructor(bikes: number) {
    this.bikesPerTimeList = new Array();
    let value: BikesPerTime = {time: 0, availableBikes: bikes};
    this.bikesPerTimeList.push(value);
  }
  
  public addBike(time: number): void {
    let lastPos: number = this.bikesPerTimeList.length-1;
    let bikes: number = this.bikesPerTimeList[lastPos].availableBikes; 
    let value: BikesPerTime = {time: time, availableBikes: ++bikes};
    this.bikesPerTimeList.push(value);
  }
  
  public substractBike(time: number): void {
    let lastPos: number = this.bikesPerTimeList.length-1;
    let bikes: number = this.bikesPerTimeList[lastPos].availableBikes; 
    let value: BikesPerTime = {time: time, availableBikes: --bikes};
    this.bikesPerTimeList.push(value);
  }
}

export class BikesPerStationInfo implements Observer {
  private stations: Map<number, StationBikesPerTimeList>;
  private reservations: Array<Reservation>; 
  
  public async init(path: string): Promise<void> {
      try {
          let history: HistoryReader = await HistoryReader.create(path);
          
          let reservationEntities: HistoryEntitiesJson = await history.getEntities('reservations');
          this.reservations = <Reservation[]> reservationEntities.instances;
          
          let stationEntities: HistoryEntitiesJson = await history.getEntities('stations');
          let stationInstances = <Station[]> stationEntities.instances;
        
          for(let station of stationInstances) {
              
            let value: StationBikesPerTimeList = new StationBikesPerTimeList(this.obtainInitAvailableBikesOf(station));
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
      
    let lastPos, reservationId: number;
    let reservation: Reservation;
      
    for(let event of events) {
      let eventStations: Array<Station> = event.changes.stations;
        
      switch(event.name) {
        case "EventUserAppears": {
          if (eventStations !== undefined) {
            for(let station of eventStations) {
              // Getting the last bike reservation which has happend at each station
              lastPos = station.reservations.id.length-1;
              reservationId = station.reservations.id[lastPos];
              reservation = this.getReservation(reservationId);
                
              // If gotten reservation is active, it means the number of available bikes has decreased
              if (reservation.state === "ACTIVE") {  // reservationtype = BIKE
                // Getting the number of available bikes at the time the UserAppears 
                // event has happened to update it (it's decreased).  
                this.stations.get(station.id).substractBike(instant);
              }
            }
          }
          break;
        }
          
        case "EventUserArrivesAtStationToRentBikeWithoutReservation": {
            if (eventStations.length > 0) {
               for(let station of eventStations) {
                   // If bike ids have been registered, it means a change has 
                   // occurred (there's one bike less) -> update number of bikes
                   if (station.bikes !== undefined) {
                        this.stations.get(station.id).substractBike(instant);
                   }
                   
                    else if (station.reservations !== undefined) { 
                        // Getting the last bike reservation which has happend at each station
                        lastPos = station.reservations.id.length-1;
                        reservationId = station.reservations.id[lastPos];
                        reservation = this.getReservation(reservationId);
                
                        // If gotten reservation is active, it means the number of available bikes has decreased
                        if (reservation.state === "ACTIVE") {  // reservationtype = BIKE
                            // Getting the number of available bikes at the time the UserAppears 
                            // event has happened to update it (it's decreased).  
                            this.stations.get(station.id).substractBike(instant);
                        }
                    }
                }
            }
            break;
        }
          
        case "EventBikeReservationTimeout": {
            let historyReservations: Array<Reservation> = event.changes.reservations;
            for(let historyReservation of historyReservations) {
                reservation = this.getReservation(historyReservation.id);
                // As reservation state is expired, the related station has one more available bike
                let stationId: number = reservation.station.id;
                this.stations.get(stationId).addBike(instant);
            }
            break;
        }
          
        case "EventUserArrivesAtStationToReturnBikeWithReservation": {
            for (let station of eventStations) {
                this.stations.get(station.id).addBike(instant);
            }
            break;
        }
          
        case "EventUserArrivesAtStationToReturnBikeWithoutReservatioon": {
              if (eventStatiosn.length > 0) {
                  for (let station of eventStations) {
                      if (station.bikes !== undefined) {
                          this.stations.get(station.id).addBike(instant);
                      }
//                }
              }
          break;
        }
              
    }
  }
    
  private obtainInitAvailableBikesOf(station: Station): number {
    let counter: number = 0;
    station.bikes.id.forEach( (bikeId) => {
        if (bikeId !== null) { 
            counter++;
        }
    });
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
