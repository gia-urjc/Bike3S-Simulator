import { Reservation, ReservationType, ReservationState } from '../../../dataTypes/Entities';

// in all maps, key = user id

export class BikeFailedReservations {
    private bikeFailedReservationsPerUser: Map<number, number>;
    
    public getMap(): Map<number, number> {
        return this.bikeFailedReservationsPerUser;
    }
    
    public update(reservation: Reservation): void {
        let key: number = reservation.user.id;
        let value: number | undefined;
        
        if (reservation.type === "ReservationType.BIKE" && reservation.state === "ReservationState.FAILED") {
            value = this.bikeFailedReservationsPerUser.get(key);
            if (value !== undefined) {
                this.bikeFailedReservationsPerUser.set(key, ++value);
            }
            else {
                this.bikeFailedReservationsPerUser.set(key, 0);
            }
        }
    }
}
    
export class SlotFailedReservations { 
    private slotFailedReservationsPerUser: Map<number, number>;
    
    public getMap(): Map<number, number> {
        return this.slotFailedReservationsPerUser;
    }
    
    public update(reservation: Reservation): void {
        let key: number = reservation.user.id;
        let value: number | undefined;
                
        if (reservation.type === "ReservationType.SLOT" && reservation.state === "ReservationState.FAILED") {
            value = this.slotFailedReservationsPerUser.get(key);
            if (value !== undefined) {                 
                this.slotFailedReservationsPerUser.set(key, ++value);
            }
            else {
                this.slotFailedReservationsPerUser.set(key, 0);
            }
        }
    }
    
}

export class BikeSuccessfulReservations {
    private bikeSuccessfulReservationsPerUser: Map<number, number>;
    
    public getMap(): Map<number, number> {
        return this.bikeSuccessfulReservationsPerUser;
    }
    
    public update(reservation: Reservation): void {
        let key: number = reservation.user.id;
        let value: number | undefined;
        
        if (reservation.type === "ReservationType.BIKE" && reservation.state === "ReservationState.SUCCESSFUL") {
            value = this.bikeSuccessfulReservationsPerUser.get(key);
            if (value !== undefined) {
                this.bikeSuccessfulReservationsPerUser.set(key, ++value);
            }
            else {
                this.bikeSuccessfulReservationsPerUser.set(key, 0);
            }
        }     
    } 

}

export class SlotSuccessfulReservations { 
    private slotSuccessfulReservationsPerUser: Map<number, number>;
    
    public getMap(): Map<number, number> {
        return this.slotSuccessfulReservationsPerUser;
    }
    
    public update(reservation: Reservation): void {
        let key: number = reservation.user.id;
        let value: number | undefined;
        
        if (reservation.type === "ReservationType.SLOT" && reservation.state === "ReservationState.SUCCESSFUL") {
            value = this.slotSuccessfulReservationsPerUser.get(key);
            if (value !== undefined) {                                
                this.slotSuccessfulReservationsPerUser.set(key, ++value);
            }
            else {
                this.slotSuccessfulReservationsPerUser.set(key, 0);                
            }
        }
    }
    
}
