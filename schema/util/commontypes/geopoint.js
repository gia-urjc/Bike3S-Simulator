const { SObject, SNumber } = require('../jsonschema/types');
const { RequireAll } = require('../jsonschema/constraints');

module.exports = SObject({
    latitude: SNumber(),
    longitude: SNumber(),
}, RequireAll());