const { sObject, sNumber } = require('../jsonschema/types');
const { requireAll } = require('../jsonschema/constraints');

module.exports = sObject({
    latitude: sNumber(),
    longitude: sNumber(),
}, requireAll());