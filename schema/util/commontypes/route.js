const { sNumber, sObject, sArray } = require('../jsonschema/types');
const { xMin, requireAll } = require('../jsonschema/constraints');

const GeoPoint = require('./geopoint');

module.exports = sObject({
    totalDistance: sNumber(xMin(0)),
    points: sArray(GeoPoint),
    intermediateDistances: sArray(sNumber(xMin(0))),
}, requireAll());