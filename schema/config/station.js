const { SInteger, SObject, SArray } = require('../util/jsonschema');
const { Min, RequireAll } = require('../util/jsonschema/constraints');
const GeoPoint = require('../util/customtypes/geopoint');

const Bike = SObject({});

module.exports = SObject({
    position: GeoPoint,
    capacity: SInteger(Min(0)),
    bikes: {
        oneOf: [SInteger(Min(0)), SArray(Bike)]
    }
}, RequireAll());