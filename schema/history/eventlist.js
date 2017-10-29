const { JSInteger, JSString, JSArray, JSObject, JSNull, JsonSchema } = require('../util/jsonschema');
const { Min, Require, RequireAll } = require('../util/jsonschema/constraints');
const { GeoPoint } = require('../common');

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
}, Require('id'));

// TODO: add other entities

const Event = JSObject({
    name: JSString(),
    changes: JSObject({
        users: JSArray(User)
    })
}, Require('name'));

const Entry = JSObject({
    time: JSInteger(),
    events: JSArray(Event),
}, RequireAll());

module.exports = JsonSchema(JSArray(Entry));