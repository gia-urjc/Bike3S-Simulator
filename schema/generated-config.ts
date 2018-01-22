import {} from './initial-config';
import {GeoPoint, options, UInt, UserType} from './common';
import {sArray, sInteger, sObject, sString} from 'json-schema-builder-ts/dist/types';
import {JsonSchema} from 'json-schema-builder-ts';
import {rData} from 'json-schema-builder-ts/dist/references';
import {Station} from './common-config';
import {UserProperties} from './common/users';

const SingleUser = sObject({
    userType: UserProperties,
    position: GeoPoint,
    timeInstant: UInt
});

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
    initialUsers: sArray(SingleUser),
    stations: sArray(Station),
}).require.all().restrict());

