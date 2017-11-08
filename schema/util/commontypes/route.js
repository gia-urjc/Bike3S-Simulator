const { SNumber, SObject, SArray } = require('../jsonschema/types');
const { XMin, RequireAll } = require('../jsonschema/constraints');

const GeoPoint = require('./geopoint');

module.exports = SObject({
    totalDistance: SNumber(XMin(0)),
    points: SArray(GeoPoint),
    intermediateDistances: SArray(SNumber(XMin(0))),
}, RequireAll());