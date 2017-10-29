const { JSInteger, JSObject, JSArray } = require('../util/jsonschema');
const { Min, RequireAll } = require('../util/jsonschema/constraints');
const GeoPoint = require('../common/geopoint');

const Bike = JSObject({});

module.exports = JSObject({
    position: GeoPoint,
    capacity: JSInteger(Min(0)),
    bikes: {
        oneOf: [JSInteger(Min(0)), JSArray(Bike)]
    }
}, RequireAll());