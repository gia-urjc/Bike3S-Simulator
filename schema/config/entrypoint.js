const merge = require('../util/merge');

const { JSNumber, JSInteger, JSObject, JSConst } = require('../util/jsonschema');
const { Min, XMin, Require, RequireAll } = require('../util/jsonschema/constraints');
const { GeoPoint, UserType } = require('../common');

const distributions = [
    {
        type: JSConst('RANDOM')
    },
    {
        type: JSConst('POISSON'),
        lambda: JSNumber()
    },
].map((properties) => JSObject(properties, RequireAll()));

const itemBase = JSObject({
    userType: UserType,
    position: GeoPoint,
}, RequireAll());

const validItems = [
    JSObject({
        timeInstant: JSInteger(Min(0))
    }, RequireAll()),
    JSObject({
        distribution: { oneOf: distributions },
        radius: JSNumber(XMin(0))
    }, Require('distribution')),
].map((item) => merge(item, itemBase));

module.exports = {
    oneOf: validItems
};