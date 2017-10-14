/*const _ = require('lodash');

const merge = (target, ...sources) => _.mergeWith(target, sources, (targetValue, sourceValue) => {
    if (_.isArray(targetValue)) return targetValue.concat(sourceValue);
});*/

const { merge } = require('lodash');

const ID = require('../common/id');
const GeoPoint = require('../common/geopoint');

const NULL = { type: 'null' };

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
        old: { anyOf: [ID, NULL] },
        new: { anyOf: [ID, NULL] },
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