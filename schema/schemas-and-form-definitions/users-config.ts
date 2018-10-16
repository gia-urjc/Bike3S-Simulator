import {GeoPoint, UInt} from "../common/index";
import {sBoolean, sNumber} from "json-schema-builder-ts/dist/types";

const Percentage = sNumber().min(0).max(100);

export const typeParameters = {
    USER_RANDOM: {},
    USER_UNINFORMED: {
        destinationPlace: GeoPoint
    },
    USER_INFORMED: {
        willReserve: sBoolean(),
        minReservationAttempts: UInt,
        minReservationTimeouts: UInt,
        minRentalAttempts: UInt,
        bikeReservationPercentage: Percentage,
        slotReservationPercentage: Percentage,
        reservationTimeoutPercentage: Percentage,
        failedReservationPercentage: Percentage
    },
    USER_OBEDIENT: {
        willReserve: sBoolean(),
        minReservationAttempts: UInt,
        minReservationTimeouts: UInt,
        minRentalAttempts: UInt,
        bikeReservationPercentage: Percentage,
        slotReservationPercentage: Percentage,
        reservationTimeoutPercentage: Percentage,
        failedReservationPercentage: Percentage
    },
    USER_DISTANCE_RESTRICTION: {
        minReservationAttempts: UInt,
        minReservationTimeouts: UInt,
        minRentalAttempts: UInt,
        bikeReturnPercentage: Percentage,
        reservationTimeoutPercentage: Percentage,
        failedReservationPercentage: Percentage,
        maxDistance: sNumber()
    },
    USER_REASONABLE: {
        minReservationAttempts: UInt,
        minReservationTimeOuts: UInt,
        minRentalAttempts: UInt,
        bikeReturnPercentage: Percentage,
        reservationTimeoutPercentage: Percentage,
        failedReservationPercentage: Percentage
    },
    USER_COMMUTER: {
        destinationPlace: GeoPoint,
        minReservationAttempts: UInt,
        minReservationTimeouts: UInt,
        minRentalAttempts: UInt,
        bikeReservationPercentage: Percentage,
        slotReservationPercentage: Percentage,
        cyclingVelocity: sNumber()
    },
    USER_AVAILABLE_RESOURCES: {
        minReservationAttempts: UInt,
        minReservationTimeouts: UInt,
        minRentalAttempts: UInt,
        bikeReturnPercentage: Percentage,
        reservationTimeoutPercentage: Percentage,
        failedReservationPercentage: Percentage
    },
    USER_TOURIST: {
        touristDestination: GeoPoint,
        minReservationAttempts: UInt,
        minReservationTimeOuts: UInt,
        minRentalAttempts: UInt,
        bikeReservationPercentage: Percentage,
        slotReservationPercentage: Percentage,
        reservationTimeoutPercentage: Percentage,
        failedReservationPercentage: Percentage
    }
};