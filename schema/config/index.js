const { JSInteger, JSString, JSObject, JSArray, JsonSchema, JSEnum } = require('../util/jsonschema');
const { Min, RequireAll } = require('../util/jsonschema/constraints');
const { GeoPoint } = require('../common');

const EntryPoint = require('./entrypoint');
const Station = require('./station');

const Locale = JSEnum(
    'es'
);

module.exports = {
    config: JsonSchema(JSObject({
        totalSimulationTime: JSInteger(Min(0)),
        reservationTime: JSInteger(Min(0)),
        randomSeed: JSInteger(),
        bbox: JSObject({
            northWest: GeoPoint,
            southEast: GeoPoint,
        }, RequireAll()),
        map: JSString(),
        graphhopperDirectory: JSString(),
        graphhopperLocale: Locale,
        entryPoints: JSArray(EntryPoint),
        stations: JSArray(Station),
    }, RequireAll()))
};

/*module.exports = {
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
};*/