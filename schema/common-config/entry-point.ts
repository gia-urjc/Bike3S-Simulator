import {sConst, sInteger, sNumber, sObject} from 'json-schema-builder-ts/dist/types';
import {UserProperties} from '../common/users';
import {sAnyOf} from 'json-schema-builder-ts/dist/operators/schematical';
import {GeoPoint, UInt} from '../common';

const Distributions = {
    'poisson': sObject({
        lambda: sNumber(),
    }).require.all().restrict()
};

export default sAnyOf(
    sObject({
        entryPointType: sConst('POISSON'),
        distribution: Distributions.poisson,
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