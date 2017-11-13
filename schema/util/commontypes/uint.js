const { sInteger } = require('../jsonschema/types');
const { min } = require('../jsonschema/constraints');

module.exports = sInteger(min(0));