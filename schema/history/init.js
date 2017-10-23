const ID = require('../common/id');
const GeoPoint = require('../common/geopoint');
const UserType = require('../common/userType');

const EntryPoints = require('../config/entrypoints');
const GeneralConfiguration = require('../config/general');

const User = {
    type: 'object',
    required: [],
    additionalProperties: false,
    properties: {
        id: ID,
        position: GeoPoint,
        type: UserType,
        averageWalkingVelocity: {
            type: 'number',
            exclusiveMinimum: 0,
        },
        averageCyclingVelocity: {
            type: 'number',
            exclusiveMinimum: 0,
        }
    }
};

const Bike = {
    type: 'object',
    required: ['id'],
    additionalProperties: false,
    properties: {
        id: ID
    }
};

const Station = {
    type: 'object',
    required: ['id', 'capacity', 'position', 'bikes'],
    additionalProperties: false,
    properties: {
        id: ID,
        position: GeoPoint,
        capacity: {
            type: 'integer',
            minimum: 0,
        },
        bikes: {
            type: 'array',
            items: Bike
        }
    }
};


module.exports = {
    $schema: 'http://json-schema.org/draft-06/schema#',
    type: 'object',
    required: ['entryPoints', 'configuration', 'userAppearanceEvents', 'stations'],
    additionalProperties: false,
    properties: {
        entryPoints: EntryPoints,
        configuration: GeneralConfiguration,
        userAppearanceEvents: {
            type: 'array',
            items: {
                type: 'object',
                required: ['timeInstant', 'user'],
                properties: {
                    timeInstant: {
                        type: 'integer',
                        minimum: 0,
                    },
                    user: User
                }
            }
        },
        stations: {
            type: 'array',
            items: Station
        }
    }
};