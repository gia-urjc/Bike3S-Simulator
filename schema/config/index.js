const { Schema } = require('../util/jsonschema/core');
const { SInteger, SString, SObject, SArray, SEnum } = require('../util/jsonschema/types');
const { RequireAll, Pattern } = require('../util/jsonschema/constraints');
const { UInt, GeoPoint } = require('../util/customtypes');

const EntryPoint = require('./entrypoint');
const Station = require('./station');

module.exports = {
    config: Schema(SObject({
        totalSimulationTime: UInt,
        reservationTime: UInt,
        randomSeed: SInteger(),
        bbox: SObject({
            northWest: GeoPoint,
            southEast: GeoPoint,
        }, RequireAll()),
        map: SString(Pattern(/.+\.osm^/)),
        entryPoints: SArray(EntryPoint),
        stations: SArray(Station),
    }, RequireAll()))
};