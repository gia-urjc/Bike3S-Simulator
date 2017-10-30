const { SInteger, SString, SObject, SArray, Schema, SEnum } = require('../util/jsonschema');
const { RequireAll, Pattern } = require('../util/jsonschema/constraints');
const { UInt, GeoPoint } = require('../util/customtypes');

const EntryPoint = require('./entrypoint');
const Station = require('./station');

const Locale = SEnum(
    'es'
);

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
        graphhopperDirectory: SString(),
        graphhopperLocale: Locale,
        entryPoints: SArray(EntryPoint),
        stations: SArray(Station),
    }, RequireAll()))
};