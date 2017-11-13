const { sInteger, sObject, sArray } = require('../util/jsonschema/types');
const { min, requireAll } = require('../util/jsonschema/constraints');
const { GeoPoint } = require('../util/commontypes');

const Bike = sObject({});

module.exports = sObject({
    position: GeoPoint,
    capacity: sInteger(min(0)),
    bikes: {
        oneOf: [sInteger(min(0)), sArray(Bike)]
    }
}, requireAll());