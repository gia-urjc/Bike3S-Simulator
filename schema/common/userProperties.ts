import { sOneOf } from 'json-schema-builder-ts/dist/operators/schematical';
import { sConst, sInteger, sNumber, sObject } from 'json-schema-builder-ts/dist/types';
import{ GeoPoint } from '../common';


export default sOneOf(
    sObject({
        typeName: sConst('USER_RANDOM')
    }),
    sObject({
        typeName: sConst('USER_DISTANCE_RESTRICTION'),
        parameters: sObject({
            minReservationAttempts: sInteger().min(0),
            minReservationTimeouts: sInteger().min(0),
            minRentingAttempts: sInteger().min(0),
            bikeReturnPercentage: sNumber().min(0).max(100),
            reservationTimeoutPercentage: sNumber().min(0).max(100),
            failedReservationPercentage: sNumber().min(0).max(100),
            maxDistance: sNumber()})
    }),
    sObject({
        typeName: sConst('USER_EMPLOYEE'),
        parameters: sObject({
            companyStreet: GeoPoint,
            minReservationAttempts: sInteger().min(0),
            minReservationTimeOuts: sInteger().min(0),
            minRentingAttempts: sInteger().min(0),
            bikeReservationPercentage: sNumber().min(0).max(100),
            slotReservationPercentage: sNumber().min(0).max(100)
        })
    }),
    sObject({
        typeName: sConst('USER_REASONABLE'),
        parameters: sObject({
            minReservationAttempts: sInteger().min(0),
            minReservationTimeOuts: sInteger().min(0),
            minRentingAttempts: sInteger().min(0),
            bikeReturnPercentage: sNumber().min(0).max(100),
            reservationTimeoutPercentage: sNumber().min(0).max(100),
            failedReservationPercentage: sNumber().min(0).max(100)
        })
    }),
    sObject({
        typeName: sConst('USER_STATIONS_BALANCER'),
        parameters: sObject({
            minReservationAttempts: sInteger().min(0),
            minReservationTimeOuts: sInteger().min(0),
            minRentingAttempts: sInteger().min(0),
            bikeReturnPercentage: sNumber().min(0).max(100),
            reservationTimeoutPercentage: sNumber().min(0).max(100),
            failedReservationPercentage: sNumber().min(0).max(100)
        })
    }),
    sObject({
        typeName: sConst('USER_TOURIST'),
        parameters: sObject({
            touristDestination: GeoPoint,
            minReservationAttempts: sNumber().min(0),
            minReservationTimeOuts: sNumber().min(0),
            minRentingAttempts: sNumber().min(0),
            bikeReservationPercentage: sInteger().min(0).max(100),
            slotReservationPercentage: sInteger().min(0).max(100),
            reservationTimeoutPercentage: sInteger().min(0).max(100),
            failedReservationPercentage: sInteger().min(0).max(100)
        })
    })
);
