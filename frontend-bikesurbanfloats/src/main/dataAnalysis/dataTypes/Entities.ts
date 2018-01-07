export interface Entity {
    id: number;
}

export interface User extends Entity {
    
}

export interface Station extends Entity {
    capacity: number;
    reservedBikes: number;
    reservedSlots: number;
    
}

export enum ReservationType {
    Bike, Slot
}

export enum ReservationState {
    FAILED, SUCCESSFUL
}

export interface Reservation extends Entity {
    type: ReservationType;
    state: ReservationState;
/*    userId: number;
    stationId: number;
    bikeId: number;*/
}