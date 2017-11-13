const { Schema } = require('../util/jsonschema/core');
const { sNumber, sObject, sArray, sNull } = require('../util/jsonschema/types');
const { sOr } = require('../util/jsonschema/operators');
const { xMin, requireBut, requireAll } = require('../util/jsonschema/constraints');
const { UInt, GeoPoint, UserType, ReservationState, ReservationType } = require('../util/commontypes');

const User = sObject({
    id: UInt,
    type: UserType,
    walkingVelocity: sNumber(xMin(0)),
    cyclingVelocity: sNumber(xMin(0)),
}, requireAll());

const Bike = sObject({
    id: UInt,
}, requireAll());

const Station = sObject({
    id: UInt,
    position: GeoPoint,
    capacity: UInt,
    bikes: sArray(sOr(UInt, sNull()))
}, requireAll());

const Reservation = sObject({
    id: UInt,
    startTime: UInt,
    // endTime: UInt, TODO: make sure this is not necessary
    user: UInt,
    station: UInt,
    bike: UInt,
    type: ReservationType,
    state: ReservationState
}, requireBut('bike'));

module.exports = Schema(sObject({
    users: sArray(User),
    bikes: sArray(Bike),
    stations: sArray(Station),
    reservations: sArray(Reservation)
    // it is technically possible that no reservations were made, so they are not required in the schema
}, requireBut('reservations')));