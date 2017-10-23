const GeoPoint = require('../common/geopoint');

module.exports = {
    $schema: 'http://json-schema.org/draft-06/schema#',
    type: 'object',
    required: [],
    additionalProperties: false,
    properties: {
        totalSimulationTime: {
            type: 'integer',
            minimum: 0
        },
        reservationTime: {
            type: 'integer',
            minimum: 0,
        },
        randomSeed: {
            type: 'integer'
        },
        bbox: {
            northWest: GeoPoint,
            southEast: GeoPoint
        }
    }
};