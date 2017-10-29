const { JSBoolean, JSInteger, JSString, JSArray, JSObject, JSNull, JsonSchema } = require('../util/jsonschema');
const { Min, Require, RequireAll } = require('../util/jsonschema/constraints');
const { GeoPoint, ReservationState } = require('../common');

const PropertyChange = (...types) => {
    const type = types.length === 1 ? types[0] : {
        anyOf: types
    };
    return JSObject({
        old: type,
        new: type,
    }, RequireAll());
};

const User = JSObject({
    id: JSInteger(Min(0)),
    position: PropertyChange(GeoPoint, JSNull()),
    bike: PropertyChange(JSInteger(Min(0)), JSNull()),
    destinationStation: PropertyChange(JSInteger(Min(0)), JSNull())
}, Require('id'), Min(2));

const Station = JSObject({
    id: JSInteger(Min(0)),
    bikes: PropertyChange(JSArray({
        anyOf: [JSInteger(Min(0)), JSNull()]
    }))
    // TODO: check other attributes of station
}, Require('id'), Min(2));

const Bike = JSObject({
    id: JSInteger(Min(0)),
    reserved: PropertyChange(JSBoolean()),
}, Require('id'), Min(2));

const Reservation = JSObject({
    id: JSInteger(Min(0)),
    endTime: PropertyChange(JSInteger(Min(0))),
    state: PropertyChange(ReservationState)
}, Require('id'), Min(2));

const Event = JSObject({
    name: JSString(),
    changes: JSObject({
        users: JSArray(User),
        stations: JSArray(Station),
        bikes: JSArray(Bike),
        reservations: JSArray(Reservation)
    })
}, RequireAll());

const Entry = JSObject({
    time: JSInteger(),
    events: JSArray(Event),
}, RequireAll());

module.exports = JsonSchema(JSArray(Entry));