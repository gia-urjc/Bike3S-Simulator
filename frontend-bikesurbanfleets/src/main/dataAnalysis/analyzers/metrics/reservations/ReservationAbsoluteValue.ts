import { AbsoluteValue } from "../../AbsoluteValue";

export class ReservationAbsoluteValue implements AbsoluteValue {
    static readonly NUM_ATTR: number = 4;
    successfulBikeReservations: number;
    failedBikeReservations: number;
    successfulSlotReservations: number;
    failedSlotReservations: number;
    
    public constructor() {
        this.successfulBikeReservations = 0;
        this.failedBikeReservations = 0;
        this.successfulSlotReservations = 0;
        this.failedSlotReservations = 0;
    }
    
    getAbsoluteValuesAsArray(): Array<number> {
        let array: Array<number> = new Array();
        array.push(this.successfulBikeReservations);
        array.push(this.failedBikeReservations);
        array.push(this.successfulSlotReservations);
        array.push(this.failedSlotReservations);
        return array;
    }
    
}