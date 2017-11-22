const { Schema } = require('../util/jsonschema/core');
const { sInteger, sString, sObject, sArray } = require('../util/jsonschema/types');
const { requireAll, pattern } = require('../util/jsonschema/constraints');
const { UInt, GeoPoint } = require('../util/commontypes');

const EntryPoint = require('./entrypoint');
const Station = require('./station');

module.exports = {
    config: Schema(sObject({
        totalSimulationTime: UInt,
        reservationTime: UInt,
        randomSeed: sInteger(),
        boundingBox: sObject({
            northWest: GeoPoint,
            southEast: GeoPoint,
        }, requireAll()),
        map: sString(pattern(/.+\.osm^/)),
        historyOutputPath: sString(),
        entryPoints: sArray(EntryPoint),
        stations: sArray(Station),
    }, requireAll()))
};