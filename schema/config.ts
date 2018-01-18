import { JsonSchema } from 'json-schema-builder-ts';
import { sAnyOf, sMerge } from 'json-schema-builder-ts/dist/operators/schematical';
import { rData } from 'json-schema-builder-ts/dist/references';
import { sArray, sConst, sEnum, sInteger, sNull, sNumber, sObject, sString } from 'json-schema-builder-ts/dist/types';
import { GeoPoint, options, UInt, UserProperties } from './common';

const distributions = {
    'poisson': sObject({
        lambda: sNumber(),
    }).require.all().restrict()
};

const EntryPoint = sAnyOf(
    sObject({
        entryPointType: sConst('POISSON'),
        distribution: distributions.poisson,
        userType: UserProperties,
        position: GeoPoint,
        timeRange: sObject({
            start: UInt,
            end: UInt,
        }).require.all().restrict(),
        radius: sNumber().xMin(0),
        totalUsers: sInteger().xMin(0)
    }).require('entryPointType', 'userType', 'distribution', 'position'),
    sObject({
        entryPointType: sConst('SINGLEUSER'),
        userType: UserProperties,
        position: GeoPoint,
        timeInstant: UInt
    })
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
