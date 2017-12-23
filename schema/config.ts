import { JsonSchema } from 'json-schema-builder-ts';
import { sAnyOf, sMerge } from 'json-schema-builder-ts/dist/operators/schematical';
import { rData } from 'json-schema-builder-ts/dist/references';
import { sArray, sConst, sInteger, sNull, sNumber, sObject, sString } from 'json-schema-builder-ts/dist/types';
import { GeoPoint, options, UInt, UserProperties } from './common';

const distributions = [
    sObject({
        type: sConst('RANDOM'),
    }).require.all().restrict(),
    sObject({
        type: sConst('POISSON'),
        lambda: sNumber(),
    }).require.all().restrict(),
];

const EntryPointBase = sObject({
    userProperties: UserProperties,
    position: GeoPoint,
}).require.all().restrict();

const EntryPoint = sAnyOf(
    sMerge(EntryPointBase, sObject({
        timeInstant: UInt,
    }).require.all().restrict()),
    sMerge(EntryPointBase, sObject({
        distribution: sAnyOf(...distributions),
        radius: sNumber().xMin(0),
        timeRange: sObject({
            start: UInt,
            end: UInt,
        }).require.all().restrict(),
        totalUsers: sInteger().xMin(0)
    }).require('distribution').restrict()),
);

const Bike = sObject();

const Station = sObject({
    position: GeoPoint,
    capacity: UInt,
    bikes: sAnyOf(
        sInteger().min(0).max(rData('1/capacity')),
        sArray(sAnyOf(Bike, sNull())).min(rData('1/capacity')).max(rData('1/capacity'))
    ),
}).require.all().restrict();

export default new JsonSchema(options, sObject({
    totalSimulationTime: UInt,
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
