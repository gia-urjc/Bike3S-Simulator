import {GeoPoint, options, UInt} from "../common/index";
import {sArray, sBoolean, sConst, sEnum, sInteger, sNumber, sObject} from "json-schema-builder-ts/dist/types";
import {JsonSchema} from "json-schema-builder-ts";
import {sAnyOf} from "json-schema-builder-ts/dist/operators/schematical";

const Percentage = sNumber().min(0).max(100);

export const typeParameters = {
    USER_RANDOM: {},
    USER_UNINFORMED: {},
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
        minRentingAttempts: UInt,
        bikeReturnPercentage: Percentage,
        reservationTimeoutPercentage: Percentage,
        failedReservationPercentage: Percentage,
        maxDistance: sNumber()
    },
    USER_EMPLOYEE: {
        companyStreet: GeoPoint,
        minReservationAttempts: UInt,
        minReservationTimeOuts: UInt,
        minRentingAttempts: UInt,
        bikeReservationPercentage: Percentage,
        slotReservationPercentage: Percentage
    },
    USER_REASONABLE: {
        minReservationAttempts: UInt,
        minReservationTimeOuts: UInt,
        minRentingAttempts: UInt,
        bikeReturnPercentage: Percentage,
        reservationTimeoutPercentage: Percentage,
        failedReservationPercentage: Percentage
    },
    USER_STATIONS_BALANCER: {
        minReservationAttempts: UInt,
        minReservationTimeOuts: UInt,
        minRentingAttempts: UInt,
        bikeReturnPercentage: Percentage,
        reservationTimeoutPercentage: Percentage,
        failedReservationPercentage: Percentage
    },
    USER_TOURIST: {
        touristDestination: GeoPoint,
        minReservationAttempts: UInt,
        minReservationTimeOuts: UInt,
        minRentingAttempts: UInt,
        bikeReservationPercentage: Percentage,
        slotReservationPercentage: Percentage,
        reservationTimeoutPercentage: Percentage,
        failedReservationPercentage: Percentage
    }
};