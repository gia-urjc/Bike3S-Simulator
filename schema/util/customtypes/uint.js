const { SInteger } = require('../jsonschema/index');
const { Min } = require('../jsonschema/constraints/index');

module.exports = SInteger(Min(0));