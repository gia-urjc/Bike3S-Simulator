import { JsonSchema } from 'json-schema-builder-ts';
import { rData } from 'json-schema-builder-ts/dist/references';
import {sArray, sBoolean, sInteger, sObject, sString} from 'json-schema-builder-ts/dist/types';
import { GeoPoint, options, UInt } from './common';
import { EntryPoint, Station } from './common-config'



export default new JsonSchema(options, sObject({
    totalSimulationTime: UInt,
    debugMode: sBoolean(),
    reservationTime: sInteger().min(0).max(rData('1/totalSimulationTime')),
    randomSeed: sInteger(),
    boundingBox: sObject({
        northWest: GeoPoint,
        southEast: GeoPoint,
    }).require.all().restrict(),
    map: sString().pattern(/.+\.osm/),
    historyOutputPath: sString().pattern(/.+/),
    entryPoints: sArray(EntryPoint),
    stations: sArray(Station),
}).require.all().restrict());
