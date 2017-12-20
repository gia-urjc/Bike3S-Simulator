enum ReservationState {
    SUCCESSFUL, FAILED
}

interface Reservation {
    start: number;
    end: number;
    state: ReservationState;
}

export class BaseDataAnalysis {
    
}