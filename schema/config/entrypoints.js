const merge = require('../util/merge');

const GeoPoint = require('../common/geopoint');

const distributions = [
    {
        properties: {
            type: { const: 'random' }
        }
    },
    {
        required: ['lambda'],
        properties: {
            type: { const: 'poisson' },
            lambda: { type: 'number' }
        },
    }
];

const validItems = [
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
        required: ['distribution'],
        properties: {
            distribution: {
                oneOf: distributions.map((distribution) => merge(distribution, {
                    type: 'object',
                    additionalProperties: false,
                    required: ['type']
                })),
            }
        }
    }
];

module.exports = {
    $schema: 'http://json-schema.org/draft-06/schema#',
    type: 'array',
    items: {
        oneOf: validItems.map((item) => merge(item, {
            type: 'object',
            additionalProperties: false,
            required: ['userType', 'position'],
            properties: {
                userType: {
                    enum: [
                        'UserTest',
                    ]
                },
                position: GeoPoint,
                radio: {
                    type: 'integer',
                    minimum: 0
                }
            },
        }))
    }
};