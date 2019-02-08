import {GeoPoint, UInt} from "../common/index";
import {sBoolean, sNumber} from "json-schema-builder-ts/dist/types";

const Percentage = sNumber().min(0).max(100);

export const typeParameters = {
    USER_UNINFORMED: {
        minRentalAttempts: UInt,
        maxDistanceToRentBike: UInt,
        intermediatePosition: GeoPoint
    },
    USER_UNINFORMED_RES: {
        minRentalAttempts: UInt,
        maxDistanceToRentBike: UInt,
        intermediatePosition: GeoPoint
    },
    USER_INFORMED: {
        minRentalAttempts: UInt,
        maxDistanceToRentBike: UInt,
        intermediatePosition: GeoPoint
    },
    USER_INFORMED_RES: {
        minRentalAttempts: UInt,
        maxDistanceToRentBike: UInt,
        intermediatePosition: GeoPoint
    },
    USER_OBEDIENT: {
        minRentalAttempts: UInt,
        maxDistanceToRentBike: UInt,
        intermediatePosition: GeoPoint
    },
    USER_OBEDIENT_RES: {
        minRentalAttempts: UInt,
        maxDistanceToRentBike: UInt,
        intermediatePosition: GeoPoint
    },
    USER_TOURIST: {
        SELECTION_STATIONS_SET: UInt,
        MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION: UInt,
        touristDestination: GeoPoint,
        minReservationTimeouts: UInt,
        minRentalAttempts: UInt,
        bikeReservationPercentage: Percentage,
        slotReservationPercentage: Percentage,
        reservationTimeoutPercentage: Percentage,
        failedReservationPercentage: Percentage
    },
    USER_ECONOMIC_INCENTIVES: {
        minRentalAttempts: UInt,
        maxDistanceToRentBike: UInt,
        intermediatePosition: GeoPoint,
        COMPENSATION: UInt,
        EXTRA: UInt,
        maxDistance: UInt
    }
};