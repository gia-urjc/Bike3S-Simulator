const { SNull } = require('../jsonschema/types');
const { SOr } = require('../jsonschema/operators');
const UInt = require('./uint');

module.exports = SOr(UInt, SNull());