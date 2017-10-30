const { SObject, SNumber } = require('../jsonschema/index');
const { RequireAll } = require('../jsonschema/constraints');

module.exports = SObject({
    latitude: SNumber(),
    longitude: SNumber(),
}, RequireAll());