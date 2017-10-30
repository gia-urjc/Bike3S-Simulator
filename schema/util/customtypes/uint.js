const { SInteger } = require('../jsonschema/index');
const { Min } = require('../jsonschema/constraints');

module.exports = SInteger(Min(0));