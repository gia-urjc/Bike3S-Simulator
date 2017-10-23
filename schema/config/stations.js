const GeoPoint = require('../common/geopoint');

const Bike = {
    type: 'object',
    required: [],
    additionalProperties: false,
};

module.exports = {
    $schema: 'http://json-schema.org/draft-06/schema#',
    type: 'array',
    items: {
        type: 'object',
        required: ['bikes', 'position', 'capacity'],
        additionalProperties: false,
        properties: {
            position: GeoPoint,
            capacity: {
                type: 'integer',
                minimum: 0
            },
            bikes: {
                oneOf: [
                    {
                        type: 'integer',
                        minimum: 0,
                        // maximum should be capacity but this restriction isn't possible with json schema
                    },
                    {
                        type: 'array',
                        items: Bike
                    }
                ]
            }
        }
    }
};