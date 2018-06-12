import {GeoPoint, options, UInt} from "../common/index";
import {sConst, sInteger, sNumber, sObject} from "json-schema-builder-ts/dist/types";
import {sAnyOf} from "json-schema-builder-ts/dist/operators/schematical";
import {UserProperties} from "../common/users";

export const Distributions = {
    'poisson': sObject({
        lambda: sNumber(),
    }).require.all().restrict()
};

export const EntryPoint = sAnyOf(
    sObject({
        entryPointType: sConst('POISSON'),
        distribution: Distributions.poisson,
        userType: UserProperties,
        position: GeoPoint,
        timeRange: sObject({
            start: UInt,
            end: UInt
        }).require.all().restrict(),
        radius: sNumber().xMin(0),
        totalUsers: sInteger().xMin(0)
    }).require('entryPointType', 'userType', 'distribution', 'position'),
    sObject({
        entryPointType: sConst('SINGLEUSER'),
        userType: UserProperties,
        position: GeoPoint,
        timeInstant: UInt
    }).require('entryPointType', 'userType', 'position', 'timeInstant')
);

