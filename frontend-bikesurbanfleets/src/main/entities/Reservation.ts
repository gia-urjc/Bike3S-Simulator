export interface Reservation {
    type: ReservationType;
    state: ReservationState;
}

export enum ReservationType {
    BIKE, SLOT
}

export enum ReservationState {
    FAILED, SUCCESSFUL
}