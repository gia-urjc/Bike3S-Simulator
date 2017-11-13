const { Schema } = require('../util/jsonschema/core');
const { sBoolean, sInteger, sString, sArray, sObject, sNull } = require('../util/jsonschema/types');
const { sOr } = require('../util/jsonschema/operators');
const { min, required, requireAll } = require('../util/jsonschema/constraints');
const { UInt, GeoPoint, Route, ReservationState } = require('../util/commontypes');

const propertyChange = (type) => sObject({
    old: type,
    new: type,
}, requireAll());

const User = sObject({
    id: UInt,
    position: propertyChange(sOr(GeoPoint, sNull())),
    bike: propertyChange(sOr(UInt, sNull())),
    destinationStation: propertyChange(sOr(UInt, sNull())),
    route: propertyChange(sOr(Route, sNull())),
}, required('id'), min(2));

const Station = sObject({
    id: UInt,
    bikes: propertyChange(sArray(sOr(UInt, sNull())))
    // TODO: check other attributes of station
}, required('id'), min(2));

const Bike = sObject({
    id: UInt,
    reserved: propertyChange(sBoolean()),
}, required('id'), min(2));

const Reservation = sObject({
    id: UInt,
    endTime: propertyChange(sOr(UInt, sNull())),
    state: propertyChange(ReservationState)
}, required('id'), min(2));

const Event = sObject({
    name: sString(),
    changes: sObject({
        users: sArray(User),
        stations: sArray(Station),
        bikes: sArray(Bike),
        reservations: sArray(Reservation)
    })
}, requireAll());

const Entry = sObject({
    time: sInteger(),
    events: sArray(Event),
}, requireAll());

module.exports = Schema(sArray(Entry));