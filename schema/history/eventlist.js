const { SBoolean, SInteger, SString, SArray, SObject, SNull, SAny, Schema } = require('../util/jsonschema');
const { Min, Require, RequireAll } = require('../util/jsonschema/constraints');
const { UInt, GeoPoint, ReservationState, IdReference } = require('../util/customtypes');

const PropertyChange = (type) => SObject({
    old: type,
    new: type,
}, RequireAll());

const User = SObject({
    id: UInt,
    position: PropertyChange(SAny(GeoPoint, SNull())),
    bike: PropertyChange(IdReference),
    destinationStation: PropertyChange(IdReference)
}, Require('id'), Min(2));

const Station = SObject({
    id: UInt,
    bikes: PropertyChange(SArray(IdReference))
    // TODO: check other attributes of station
}, Require('id'), Min(2));

const Bike = SObject({
    id: UInt,
    reserved: PropertyChange(SBoolean()),
}, Require('id'), Min(2));

const Reservation = SObject({
    id: UInt,
    endTime: PropertyChange(UInt),
    state: PropertyChange(ReservationState)
}, Require('id'), Min(2));

const Event = SObject({
    name: SString(),
    changes: SObject({
        users: SArray(User),
        stations: SArray(Station),
        bikes: SArray(Bike),
        reservations: SArray(Reservation)
    })
}, RequireAll());

const Entry = SObject({
    time: SInteger(),
    events: SArray(Event),
}, RequireAll());

module.exports = Schema(SArray(Entry));