import {GeoPoint, UInt} from "../common/index";
import {sBoolean, sNumber} from "json-schema-builder-ts/dist/types";

const Percentage = sNumber().min(0).max(100);

export const typeParameters = {
    USER_RANDOM: {},
    USER_UNINFORMED: {
        minRentalAttempts: UInt
    },
    USER_DISTANCE_RESTRICTION: {
        minRentalAttempts: UInt,
        maxDistance: sNumber
    },
    USER_COMMUTER: {
        minRentalAttempts: UInt,
    },
    USER_AVAILABLE_RESOURCES: {
        minRentalAttempts: UInt
    },
    USER_INFORMED: {
        minRentalAttempts: UInt
    },
    USER_OBEDIENT: {
        minRentalAttempts: UInt
    },
    USER_PAPERAT2018_INF: {
        minRentalAttempts: UInt
    },
    USER_PAPERAT2018_UNINF: {
        minRentalAttempts: UInt
    },
    USER_PAPERAT2018_OBHOLGER: {
        minRentalAttempts: UInt
    },
    /*
    USER_ECONOMIC_INCENTIVE: {
        minRentalAttempts: UInt,
    },
    */
    USER_GENERAL:{
        willReserve: sBoolean,
        minReservationAttempts: UInt,
        minReservationTimeouts: UInt,
        minRentalAttempts: UInt,
        bikeReservationPercentage: Percentage,
        slotReservationPercentage: Percentage,
        reservationTimeoutPercentage: Percentage,
        failedReservationPercentage: Percentage
    },

    /*
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
    */
};