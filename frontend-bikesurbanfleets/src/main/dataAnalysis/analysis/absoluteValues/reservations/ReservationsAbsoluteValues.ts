import { AbsoluteValue } from "../AbsoluteValue";

export class ReservationsAbsoluteValues implements AbsoluteValue {
    successfulBikeReservations: number;
    failedBikeReservations: number;
    successfulSlotReservations: number;
    failedSlotReservations: number;
}