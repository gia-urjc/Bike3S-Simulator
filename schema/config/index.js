const { SInteger, SString, SObject, SArray, Schema, SEnum } = require('../util/jsonschema');
const { Min, RequireAll } = require('../util/jsonschema/constraints');
const { GeoPoint } = require('../util/customtypes');

const EntryPoint = require('./entrypoint');
const Station = require('./station');

const Locale = SEnum(
    'es'
);

module.exports = {
    config: Schema(SObject({
        totalSimulationTime: SInteger(Min(0)),
        reservationTime: SInteger(Min(0)),
        randomSeed: SInteger(),
        bbox: SObject({
            northWest: GeoPoint,
            southEast: GeoPoint,
        }, RequireAll()),
        map: SString(),
        graphhopperDirectory: SString(),
        graphhopperLocale: Locale,
        entryPoints: SArray(EntryPoint),
        stations: SArray(Station),
    }, RequireAll()))
};