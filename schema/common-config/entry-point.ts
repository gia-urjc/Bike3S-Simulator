import {sArray, sConst, sInteger, sNumber, sObject} from 'json-schema-builder-ts/dist/types';
import {UserProperties} from '../common/users';
import {sAnyOf} from 'json-schema-builder-ts/dist/operators/schematical';
import {GeoPoint, UInt} from '../common';
import {Percentage} from './common-config';

const Distributions = {
    'poisson': sObject({
        lambda: sNumber(),
    }).require.all().restrict()
};

const UserTypePercentage = sArray(sObject({
    percentage: Percentage,
    userType: UserProperties
}));

export default sAnyOf(
    sObject({
        entryPointType: sConst('POISSON'),
        distribution: Distributions.poisson,
        userTypeByPercentage: UserTypePercentage,
        position: GeoPoint,
        timeRange: sObject({
            start: UInt,
            end: UInt,
        }).require.all().restrict(),
        radius: sNumber().xMin(0),
        totalUsers: sInteger().xMin(0)
    }).require('entryPointType', 'userTypeByPercentage', 'distribution', 'position'),
    sObject({
        entryPointType: sConst('SINGLEUSER'),
        userType: UserProperties,
        position: GeoPoint,
        timeInstant: UInt
    })
);