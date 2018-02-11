import { HistoryReader } from '../../../../util';
import { HistoryEntitiesJson } from '../../../../../shared/history';
import { Observer } from '../../ObserverPattern';
import  { Station, Reservation } from '../../../systemDataTypes/Entities';
import { AbsoluteValue } from '../AbsoluteValue';

export class ReservationsPerStation implements Data {
    private stations: Array<Station>;
    private bikeFailedReservationsPerStation: Map<number, AbsoluteValue>; 
    private slotFailedReservationsPerStation: Map<number, AbsoluteValue>;
    private bikeSuccessfulReservationsPerStation: Map<number, AbsoluteValue>;
    private slotSuccessfulReservationsPerStation: Map<number, AbsoluteValue>;
    
    public constructor() {
        // TODO: init data attr
        this.bikeFailedReservationsPerStation = new Map<number, AbsoluteValue>(); 
        this.slotFailedReservationsPerStation = new Map<number, AbsoluteValue>();
        this.bikeSuccessfulReservationsPerStation = new Map<number, AbsoluteValue>();
        this.slotSuccessfulReservationsPerStation = new Map<number, AbsoluteValue>();
    }
    
    public async init(path: string): Promise<void> {
        try {
            let history: HistoryReader = await HistoryReader.create(path);
            let entities: HistoryEntitiesJson = await history.getEntities("stations");
            this.stations = <Station[]> entities.instances;
        
            for(let station of this.stations) {
                this.bikeFailedReservationsPerStation.set(station.id, { name: "Failed bike reservations", value: 0});
                this.slotFailedReservationsPerStation.set(station.id, { name: "Failed slot reservations", value: 0});            
                this.bikeSuccessfulReservationsPerStation.set(station.id, { name: "Successfulbike reservations", value: 0});
                this.slotSuccessfulReservationsPerStation.set(station.id, { name: "Successful slot reservations", value: 0});
            }
        }
        catch(error) {
            console.log('error getting stations when initializing reservations values:', error);
        }
        return;
    }
   
    public static async create(path: string): Promise<ReservationsPerStation> {
        let reservationValues = new ReservationsPerStation();
        await reservationValues.init(path);
        return reservationValues;
    }
    
    public getBikeFailedReservationsOfStation(stationId: number): number | undefined {
     let absoluteValue:  AbsoluteValue = this.bikeFailedReservationsPerStation.get(stationId);
        return absoluteValue.value;
    }

    public getSlotFailedReservationsOfStation(stationId: number): number | undefined {
        let absoluteValue:  AbsoluteValue = this.slotFailedReservationsPerStation.get(stationId);
        return absoluteValue.value;
    }
    
    public getBikeSuccessfulReservationsOfStation(stationId: number): number | undefined {
        let absoluteValue:  AbsoluteValue = this.bikeSuccessfulReservationsPerStation.get(stationId);
        return absoluteValue.value;
    }
    
    public getSlotSuccessfulReservationsOfStation(stationId: number): number | undefined {
        let absoluteValue:  AbsoluteValue = this.slotSuccessfulReservationsPerStation.get(stationId);
        return absoluteValue.value;
    }
        
    public update(reservation: Reservation): void {
        let key: number = reservation.station.id;
        let absoluteValue: AbsoluteValue | undefined;
        
        if (reservation.type === 'BIKE' && reservation.state === 'FAILED') {
            absoluteValue = this.bikeFailedReservationsPerStation.get(key);
            if (absoluteValue !== undefined) {
                absoluteValue.value++;
            }
        }
        else if (reservation.type === 'SLOT' && reservation.state === 'FAILED') {
            absoluteValue = this.slotFailedReservationsPerStation.get(key);
            if (absoluteValue !== undefined) {
                absoluteValue.value++;
            }
        }
        else if (reservation.type === 'BIKE' && reservation.state === 'ACTIVE') {
            absoluteValue = this.bikeSuccessfulReservationsPerStation.get(key);
            if (absoluteValue !== undefined) {
                absoluteValue.value++;
            }
        }
        else if (reservation.type === 'SLOT' && reservation.state === 'ACTIVE') {
            absoluteValue = this.slotSuccessfulReservationsPerStation.get(key);
            if (absoluteValue !== undefined) {
                absoluteValue.value++;
            }
        }
    }
  
  public toString(type: string): string {
      let str: string = "";
      switch(type) {
          case "Failed bike reservations": {
              this.bikeFailedReservationsPerStation.forEach( (absoluteValue, key) => 
                  src+= 'Station '+key+' '+absoluteValue.name+': '+absoluteValue.value);
              break;
          }
              
          case "Successful bike reservations": {
              this.bikeSuccessfulReservationsPerStation.forEach( (absoluteValue, key) =>
                  src+= 'Station '+key+' '+absoluteValue.name+': '+absoluteValue.value);
              break; 
          }
              
          case "Failed slot reservations": {
              this.slotFailedReservationsPerStation.forEach( (absoluteValue, key) =>
                  src+= 'Station '+key+' '+absoluteValue.name+': '+absoluteValue.value);
              break; 
          }
              
          case "Successful slot reservations": {
              this.slotSuccessfulReservationsPerStation.forEach( (absoluteValue, key) =>
                  src+= 'Station '+key+' '+absoluteValue.name+': '+absoluteValue.value);
              break; 
          }
      }
  }
  
  public getBikeSuccessfulReservations(): Map<number, AbsoluteValue> {
    return this.bikeSuccessfulReservationsPerStation;
  }
   
   public getSlotSuccessfulReservations(): Map<number, AbsoluteValue> {
     return this.slotSuccessfulReservationsPerStation;
   }
  
  public getBikeFailedReservations(): Map<number, AbsoluteValue> {
    return this.bikeFailedReservationsPerStation;
  }
  
  public getSlotFailedReservations(): Map<number, AbsoluteValue> {
    return this.slotFailedReservationsPerStation;
  }
              
}