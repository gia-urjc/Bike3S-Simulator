const merge = require('../util/merge');

const ID = require('../common/id');
const GeoPoint = require('../common/geopoint');

const entityChangeBase = {
    type: 'object',
    required: ['id', 'changes'],
    additionalProperties: false,
    properties: {
        id: ID,
        changes: {
            type: 'object',
            additionalProperties: false,
        }
    }
};

const propertyChangeBase = {
    type: 'object',
    required: ['old', 'new'],
    additionalProperties: false,
};

const idReference = merge({
    properties: {
        old: { anyOf: [ID, { type: 'null' }] },
        new: { anyOf: [ID, { type: 'null' }] },
    }
}, propertyChangeBase);

module.exports = {
    $schema: 'http://json-schema.org/draft-06/schema#',
    type: 'array',
    items: {
        type: 'object',
        required: ['users'],
        additionalProperties: false,
        properties: {
            users: {
                type: 'array',
                items: merge({
                    properties: {
                        changes: {
                            properties: {
                                bike: idReference,
                                position: merge({
                                    properties: {
                                        old: GeoPoint,
                                        new: GeoPoint,
                                    }
                                }, propertyChangeBase)
                            }
                        }
                    }
                }, entityChangeBase)
            },
            stations: {
                type: 'array',
                items: merge({
                    properties: {
                        changes: {
                            properties: {
                                bikes: {
                                    type: 'object',
                                    additionalProperties: false,
                                    patternProperties: {
                                        // regex for unsigned integers, key refers to position in bike array
                                        '^([1-9][0-9]*|0)$': idReference
                                    }
                                }
                            }
                        }
                    }
                }, entityChangeBase)
            }
        }
    }
};