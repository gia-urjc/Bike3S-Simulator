import { JsonSchema } from 'json-schema-builder-ts';
import { sAnyOf } from 'json-schema-builder-ts/dist/operators/schematical';
import { rData } from 'json-schema-builder-ts/dist/references';
import { sArray, sNull, sNumber, sObject } from 'json-schema-builder-ts/dist/types';
import { GeoPoint, options, ReservationState, ReservationType, UInt, UserType } from './common';

const User = sObject({
    id: UInt,
    type: UserType,
    walkingVelocity: sNumber().xMin(0),
    cyclingVelocity: sNumber().xMin(0),
}).require.all().restrict();

const Bike = sObject({
    id: UInt,
}).require.all().restrict();

const Station = sObject({
    id: UInt,
    position: GeoPoint,
    capacity: UInt,
    bikes: sArray(sAnyOf(UInt, sNull()))
        .minItems(rData('1/capacity'))
        .maxItems(rData('1/capacity')),
}).require.all().restrict();

const Reservation = sObject({
    id: UInt,
    startTime: UInt,
    user: UInt,
    station: UInt,
    bike: UInt,
    type: ReservationType,
    state: ReservationState,
}).require.but('bike').restrict();

export default new JsonSchema(options, sObject({
    users: sArray(User),
    bikes: sArray(Bike),
    stations: sArray(Station),
    reservations: sArray(Reservation),
}).require.but('reservations').restrict());
