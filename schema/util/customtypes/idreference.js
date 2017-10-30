const { SNull, SAny } = require('../jsonschema/index');
const UInt = require('./uint');

module.exports = SAny(UInt, SNull());