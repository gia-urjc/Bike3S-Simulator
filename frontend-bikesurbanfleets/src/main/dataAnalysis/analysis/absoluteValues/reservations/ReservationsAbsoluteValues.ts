import { AbsoluteValue } from "../AbsoluteValue";

export class ReservationsAbsoluteValues implements AbsoluteValue {
    successfulBikeReservations: number;
    failedBikeReservations: number;
    successfulSlotReservations: number;
    failedSlotReservations: number;
    
    getAbsoluteValuesAsArray(): Array<number> {
        let array: Array<number> = new Array();
        array.push(this.successfulBikeReservations);
        array.push(this.failedBikeReservations);
        array.push(this.successfulSlotReservations);
        array.push(this.failedSlotReservations);
        return array;
    }
    
}