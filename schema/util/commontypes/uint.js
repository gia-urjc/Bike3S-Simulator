const { SInteger } = require('../jsonschema/types');
const { Min } = require('../jsonschema/constraints');

module.exports = SInteger(Min(0));