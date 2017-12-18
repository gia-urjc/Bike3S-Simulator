import { JsonSchema } from 'json-schema-builder-ts';
import { sAnyOf } from 'json-schema-builder-ts/dist/operators/schematical';
import { rData } from 'json-schema-builder-ts/dist/references';
import { sArray, sNull, sNumber, sObject } from 'json-schema-builder-ts/dist/types';
import { GeoPoint, idReference, options, ReservationState, ReservationType, UInt, UserType } from './common';

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
    bikes: idReference(
        'bikes',
        sArray(sAnyOf(UInt, sNull()))
            .minItems(rData('2/capacity'))
            .maxItems(rData('2/capacity'))
    ),
}).require.all().restrict();

const Reservation = sObject({
    id: UInt,
    startTime: UInt,
    user: idReference('users'),
    station: idReference('stations'),
    bike: sAnyOf(idReference('bikes'), sNull()),
    type: ReservationType,
    state: ReservationState,
}).require.all().restrict();

export default new JsonSchema(options, sObject({
    users: sArray(User),
    bikes: sArray(Bike),
    stations: sArray(Station),
    reservations: sArray(Reservation),
}).require.but('reservations').restrict());
