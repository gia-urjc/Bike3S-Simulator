module.exports = {
    type: 'object',
    required: ['latitude', 'longitude'],
    additionalProperties: false,
    properties: {
        latitude: { type: 'number' },
        longitude: { type: 'number' }
    },
};