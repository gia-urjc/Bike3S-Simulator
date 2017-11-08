const { Schema } = require('../util/jsonschema/core');
const { SBoolean, SInteger, SString, SArray, SObject, SNull } = require('../util/jsonschema/types');
const { SOr } = require('../util/jsonschema/operators');
const { Min, Require, RequireAll } = require('../util/jsonschema/constraints');
const { UInt, GeoPoint, ReservationState } = require('../util/commontypes');

const PropertyChange = (type) => SObject({
    old: type,
    new: type,
}, RequireAll());

const User = SObject({
    id: UInt,
    position: PropertyChange(SOr(GeoPoint, SNull())),
    bike: PropertyChange(SOr(UInt, SNull())),
    destinationStation: PropertyChange(SOr(UInt, SNull()))
}, Require('id'), Min(2));

const Station = SObject({
    id: UInt,
    bikes: PropertyChange(SArray(SOr(UInt, SNull())))
    // TODO: check other attributes of station
}, Require('id'), Min(2));

const Bike = SObject({
    id: UInt,
    reserved: PropertyChange(SBoolean()),
}, Require('id'), Min(2));

const Reservation = SObject({
    id: UInt,
    endTime: PropertyChange(SOr(UInt, SNull())),
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