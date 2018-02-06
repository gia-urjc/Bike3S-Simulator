import { sAnyOf } from 'json-schema-builder-ts/dist/operators/schematical';
import { sConst, sEnum, sInteger, sNumber, sObject } from 'json-schema-builder-ts/dist/types';
import { GeoPoint, UInt } from './index';

const Percentage = sNumber().min(0).max(100);

const typeParameters = {
    USER_RANDOM: {},
    USER_UNINFORMED: {},
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

export const UserType = sEnum(...Object.keys(typeParameters));

export const UserProperties = sAnyOf(...Object.entries(typeParameters).map((user) => {
    const type = user[0];
    const parameters: any = user[1];
    return sObject({
        typeName: sConst(type),
        parameters: parameters && sObject(parameters)
    })
}));
