import {GeoPoint, options, UInt} from "../common/index";
import {sArray, sBoolean, sConst, sEnum, sInteger, sNumber, sObject} from "json-schema-builder-ts/dist/types";

const Percentage = sNumber().min(0).max(100);

export const typeParameters = {
    USER_RANDOM: {},
    USER_UNINFORMED: {
        destinationPlace: GeoPoint
    },
    USER_INFORMED: {
        willReserve: sBoolean(),
        destinationPlace: GeoPoint,
        minReservationAttempts: UInt,
        minReservationTimeouts: UInt,
        minRentalAttempts: UInt,
        minArrivalTimeToReserveAtSameStation: UInt,
        bikeReservationPercentage: Percentage,
        slotReservationPercentage: Percentage,
        reservationTimeoutPercentage: Percentage,
        failedReservationPercentage: Percentage
    },
    USER_OBEDIENT: {
        willReserve: sBoolean(),
        destinationPlace: GeoPoint,
        minReservationAttempts: UInt,
        minReservationTimeouts: UInt,
        minRentalAttempts: UInt,
        bikeReturnPercentage: Percentage,
        reservationTimeoutPercentage: Percentage,
        failedReservationPercentage: Percentage
    },
    USER_DISTANCE_RESTRICTION: {
        destinationPlace: sBoolean(),
        minArrivalTimeToReserveAtSameStation: UInt,
        minReservationAttempts: UInt,
        minReservationTimeouts: UInt,
        minRentalAttempts: UInt,
        bikeReturnPercentage: Percentage,
        reservationTimeoutPercentage: Percentage,
        failedReservationPercentage: Percentage,
        maxDistance: sNumber()
    },
    USER_COMMUTER: {
        destinationPlace: GeoPoint,
        minArrivalTimeToReserveAtSameStation: UInt,
        minReservationAttempts: UInt,
        minReservationTimeouts: UInt,
        minRentalAttempts: UInt,
        bikeReservationPercentage: Percentage,
        slotReservationPercentage: Percentage,
        cyclingVelocity: sNumber()
    },
    USER_AVAILABLE_RESOURCES: {
        destinationPlace: GeoPoint,
        minArrivalTimeToReserveAtSameStation: UInt,
        minReservationAttempts: UInt,
        minReservationTimeouts: UInt,
        minRentalAttempts: UInt,
        bikeReturnPercentage: Percentage,
        reservationTimeoutPercentage: Percentage,
        failedReservationPercentage: Percentage
    },
    USER_TOURIST: {
        touristDestination: GeoPoint,
        minArrivalTimeToReserveAtSameStation: GeoPoint,
        minReservationAttempts: UInt,
        minReservationTimeOuts: UInt,
        minRentalAttempts: UInt,
        bikeReservationPercentage: Percentage,
        slotReservationPercentage: Percentage,
        reservationTimeoutPercentage: Percentage,
        failedReservationPercentage: Percentage
    },
    USER_ECONOMIC_INCENTIVES: {
        willReserve: sBoolean(),
        destinationPlace: GeoPoint,
        minReservationAttempts: UInt,
        minReservationTimeouts: UInt,
        minRentalAttempts: UInt,
        bikeReturnPercentage: Percentage,
        reservationTimeoutPercentage: Percentage,
        failedReservationPercentage: Percentage,
    }
};
