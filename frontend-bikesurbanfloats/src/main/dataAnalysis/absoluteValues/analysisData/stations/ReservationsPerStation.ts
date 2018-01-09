import { HistoryReader } from '../../../../util';
import { HistoryEntitiesJson } from '../../../../../shared/History';
import { Observer } from '../../ObserverPattern';
import  { Station, Reservation, ReservationType, ReservationState  } from '../../../systemDataTypes/Entities';

export class ReservationsPerStation implements Observer {
    private stations: Array<Station>;
    private bikeFailedReservationsPerStation: Map<number, number>; 
    private slotFailedReservationsPerStation: Map<number, number>;
    private bikeSuccessfulReservationsPerStation: Map<number, number>;
    private slotSuccessfulReservationsPerStation: Map<number, number>;
    
    private constructor() {
        this.bikeFailedReservationsPerStation = new Map<number, number>(); 
        this.slotFailedReservationsPerStation = new Map<number, number>();
        this.slotFailedReservationsPerStation = new Map<number, number>();
        this.slotSuccessfulReservationsPerStation = new Map<number, number>();
    }
    
    private async init(path: string): Promise<void> {
        let history: HistoryReader = await HistoryReader.create(path);
        let entities: HistoryEntitiesJson = await history.getEntities("stations");
        this.stations = entities.stations;
        
        for(let station of this.stations) {
            this.bikeFailedReservationsPerStation.set(station.id, 0);
            this.slotFailedReservationsPerStation.set(station.id, 0);            
            this.bikeSuccessfulReservationsPerStation.set(station.id, 0);
            this.slotSuccessfulReservationsPerStation.set(station.id, 0);
        }
    }
   
    public static async create(path: string): Promise<ReservationsPerStation> {
        let reservationValues = new ReservationsPerStation();
        await reservationValues.init(path);
        return reservationValues;
    }
    
    public getBikeFailedReservationsOfStation(stationId: number): number| undefined {
     return this.bikeFailedReservationsPerStation.get(stationId);
    }

    public getSlotFailedReservationsOfStation(stationId: number): number | undefined {
        return this.slotFailedReservationsPerStation.get(stationId);
    }
    
    public getBikeSuccessfulReservationsOfStation(stationId: number): number | undefined {
        return this.bikeSuccessfulReservationsPerStation.get(stationId);
    }
    
    public getSlotSuccessfulReservationsOfStation(stationId: number): number | undefined {
        return this.slotSuccessfulReservationsPerStation.get(stationId);
    }
        
    public update(reservation: Reservation): void {
        let key: number = reservation.station.id;
        let value: number | undefined;
        
        if (reservation.type === ReservationType.BIKE && reservation.state === ReservationState.FAILED) {
            value = this.bikeFailedReservationsPerStation.get(key);
            if (value !== undefined) {
                this.bikeFailedReservationsPerStation.set(key, ++value);
            }
        }
        else if (reservation.type === ReservationType.SLOT && reservation.state === ReservationState.FAILED) {
            value = this.slotFailedReservationsPerStation.get(key);
            if (value !== undefined) {                 
                this.slotFailedReservationsPerStation.set(key, ++value);
            }
        }
        else if (reservation.type === ReservationType.BIKE && reservation.state === ReservationState.SUCCESSFUL) {
            value = this.bikeSuccessfulReservationsPerStation.get(key);
            if (value !== undefined) {
                this.bikeSuccessfulReservationsPerStation.set(key, ++value);
            }
        }
        else if (reservation.type === ReservationType.SLOT && reservation.state === ReservationState.SUCCESSFUL) {
            value = this.slotSuccessfulReservationsPerStation.get(key);
            if (value !== undefined) {                                
                this.slotSuccessfulReservationsPerStation.set(key, ++value);
            }
        }

    }
              
}