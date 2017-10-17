const GeoPoint = require('../common/geopoint');

module.exports = {
    $schema: 'http://json-schema.org/draft-06/schema#',
    type: 'array',
    items: {
        type: 'object',
        required: ['userType', 'position'],
        additionalProperties: false,
        properties: {
            userType: {
                enum: [
                    'UserTest',
                ]
            },
            position: GeoPoint
        },
        oneOf: [
            {
                required: ['timeInstant'],
                properties: {
                    timeInstant: {
                        type: 'integer',
                        minimum: 0
                    }
                },
            },
            {
                distribution: {
                    type: 'object',
                    required: ['type'],
                    additionalProperties: false,
                    oneOf: [
                        {
                            properties: {
                                type: { const: 'random' }
                            }
                        },
                        {
                            properties: {
                                type: { const: 'poisson' },
                                lambda: { type: 'number' }
                            },
                            required: ['lambda']
                        }
                    ],
                }
            }
        ],
    }
};