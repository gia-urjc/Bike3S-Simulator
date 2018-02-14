import { HistoryReader } from '../../../../../../util';
import { HistoryEntitiesJson } from '../../../../../../../shared/history';
import { Observer } from '../../../../ObserverPattern';
import { Station, Reservation } from '../../../../../systemDataTypes/Entities';
import { TimeEntry, Event } from '../../../../../systemDataTypes/SystemInternalData';
import { Data } from "../Data";

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

export class BikesPerStationInfo implements Data {
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
    let station: Station;
      
    for(let event of events) {
      let eventStations: Array<Station> = event.changes.stations;
        
      switch(event.name) {
        case "EventUserAppears": {
          if (eventStations !== undefined) {
            // If there are several bike reservations, only the last can be a ctive
            station = eventStations[eventStations.length-1];
            lastPos = station.reservations.id.length-1;
            reservationId = station.reservations.id[lastPos];
            reservation = this.getReservation(reservationId);
               
            if (reservation.state === "ACTIVE") {  // and, of course, reservationtype = BIKE
                // Decreasing available bikes at the time the UserAppears event's happened 
                this.stations.get(station.id).substractBike(instant);
            }
          }
          break;
        }
          
        case "EventUserArrivesAtStationToRentBikeWithoutReservation": {
            if (eventStations.length > 0) {
                station = eventStations[0];
            
                // If bike ids have been registered, it means a change has 
                // occurred (there's one bike less) -> update number of bikes
                if (station.bikes !== undefined) {  // rental + mayvbe, slot reservations
                    this.stations.get(station.id).substractBike(instant);
                    // TODO: if rental has been possible, sure that bike reservations have not been made
                }
                   
                else { // (station.reservations !== undefined) -> bike reservations (NOT rental)
                    // Getting the last changed station registered, wihich can conatins an active bike reservation  
                    station = eventStations[eventStations.length-1];
                    lastPos = station.reservations.id.length-1;
                    reservationId = station.reservations.id[lastPos];
                    reservation = this.getReservation(reservationId);
                
                    if (reservation.state === "ACTIVE") {  // and, of course, reservation.type === "BIKE"  
                        // Decreasing available bikes at the time the UserAppears event''s happened  
                        this.stations.get(station.id).substractBike(instant);
                    }
                }
            }
            break;
        }
          
        case "EventBikeReservationTimeout": {
            let historyReservations: Array<Reservation> = event.changes.reservations;
            reservation = this.getReservation(historyReservations[0].id);
            // As reservation state is expired, the iinvolved station has one more available bike
            let stationId: number = reservation.station.id;
            this.stations.get(stationId).addBike(instant);
            break;
        }
          
        case "EventUserArrivesAtStationToReturnBikeWithReservation": {
            // only a station can have registered changes (the one the user's reached)
            station = eventStations[0];
            this.stations.get(station.id).addBike(instant);
            break;
        }
          
        case "EventUserArrivesAtStationToReturnBikeWithoutReservatioon": {
            if (eventStatiosn.length > 0) {
                station = eventStations[0];
                if (station.bikes !== undefined) {
                    this.stations.get(station.id).addBike(instant);
                }
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
  
  // TODO: should it return undefined if id doesn't exist?
  private getReservation(id: number): Reservation {
    for(let reservation of this.reservations) {
      if (reservation.id === id) {
        return reservation;
      }
    }
  }

}
