import { JsonSchema } from 'json-schema-builder-ts';
import { SchemaBuilder } from 'json-schema-builder-ts/dist/core/builder';
import { Schema } from 'json-schema-builder-ts/dist/core/builder/schema';
import { sAnyOf } from 'json-schema-builder-ts/dist/operators/schematical';
import { sArray, sBoolean, sInteger, sNull, sObject, sString } from 'json-schema-builder-ts/dist/types';
import { GeoPoint, idReference, options, ReservationState, Route, UInt } from './common';

function propertyChange(schema: Schema | SchemaBuilder) {
    return sObject({
        old: schema,
        new: schema,
    }).require.all().restrict();
}

const User = sObject({
    id: UInt,
    position: propertyChange(sAnyOf(GeoPoint, sNull())),
    bike: propertyChange(sAnyOf(idReference('bikes'), sNull())),
    destinationStation: propertyChange(sAnyOf(idReference('stations'), sNull())),
    route: propertyChange(sAnyOf(Route, sNull())),
}).require('id').minProperties(2).restrict();

const Station = sObject({
    id: UInt,
    bikes: propertyChange(idReference('bikes', sArray(sAnyOf(UInt, sNull())))),
}).require('id').minProperties(2).restrict();

const Bike = sObject({
    id: UInt,
    reserved: propertyChange(sBoolean()),
}).require('id').minProperties(2).restrict();

const Reservation = sObject({
    id: UInt,
    endTime: propertyChange(sAnyOf(UInt, sNull())),
    state: propertyChange(ReservationState),
}).require('id').minProperties(2).restrict();

const EventEntry = sObject({
    name: sString(),
    changes: sObject({
        users: sArray(User),
        stations: sArray(Station),
        bikes: sArray(Bike),
        reservations: sArray(Reservation)
    }).restrict(),
}).require.all().restrict();

const TimeEntry = sObject({
    time: sInteger(),
    events: sArray(EventEntry),
}).require.all().restrict();

export default new JsonSchema(options, sArray(TimeEntry));
