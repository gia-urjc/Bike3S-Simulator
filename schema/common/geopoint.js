const { JSObject, JSNumber } = require('../util/jsonschema');
const { RequireAll } = require('../util/jsonschema/constraints');

module.exports = JSObject({
    latitude: JSNumber(),
    longitude: JSNumber(),
}, RequireAll());