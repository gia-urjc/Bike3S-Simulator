const { Schema } = require('../util/jsonschema/core');
const { SNumber, SObject, SArray, SNull } = require('../util/jsonschema/types');
const { SOr } = require('../util/jsonschema/operators');
const { XMin, RequireBut, RequireAll } = require('../util/jsonschema/constraints');
const { UInt, GeoPoint, UserType, ReservationState, ReservationType } = require('../util/commontypes');

const User = SObject({
    id: UInt,
    type: UserType,
    walkingVelocity: SNumber(XMin(0)),
    cyclingVelocity: SNumber(XMin(0)),
}, RequireAll());

const Bike = SObject({
    id: UInt,
}, RequireAll());

const Station = SObject({
    id: UInt,
    position: GeoPoint,
    capacity: UInt,
    bikes: SArray(SOr(UInt, SNull()))
}, RequireAll());

const Reservation = SObject({
    id: UInt,
    startTime: UInt,
    // endTime: UInt, TODO: make sure this is not necessary
    user: UInt,
    station: UInt,
    bike: UInt,
    type: ReservationType,
    state: ReservationState
}, RequireBut('bike'));

module.exports = Schema(SObject({
    users: SArray(User),
    bikes: SArray(Bike),
    stations: SArray(Station),
    reservations: SArray(Reservation)
    // it is technically possible that no reservations were made, so they are not required in the schema
}, RequireBut('reservations')));