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
        positionAppearance: GeoPoint,
        destinationPlace: GeoPoint,
        timeRange: sObject({
            start: UInt,
            end: UInt
        }),
        radiusAppears: sNumber().xMin(0),
        radiusGoTo: sNumber().xMin(0),
        totalUsers: sInteger().xMin(0),
        cyclingVelocity: sNumber().xMin(0),
        walkingVelocity: sNumber().xMin(0)
    }).require('entryPointType', 'userType', 'distribution', 'positionAppearance', 'destinationPlace', 'radiusAppears', 'radiusGoTo'),
    sObject({
        entryPointType: sConst('SINGLEUSER'),
        userType: UserProperties,
        positionAppearance: GeoPoint,
        destinationPlace: GeoPoint,
        timeInstant: UInt,
    }).require('entryPointType', 'userType', 'positionAppearance', 'destinationPlace', 'timeInstant')
);

/*export const layout = 
[
    //{key: "entryPointType", placeholder: "Type of the entry point"},
    //{key: "distribution", placeholder: "Type of distribution"},
    //{key: "userType", placeholder: "Type of user"},
    {key: "position", htmlClass: 'hidden'},
    {key: "position.latitude", placeholder: "Initial latitude of the user", htmlClass: 'hidden'},
    {key: "position.longitude", placeholder: "Initial longitude of the user", htmlClass: 'hidden'},
    {key: "timeRange.start", placeholder: "Start time in seconds"},
    {key: "timeRange.end", placeholder: "End time in seconds"},
    {key: "radius", placeholder: "radius in meters"},
    {key: "totalUsers", placeholder: "Total number of users"}
];*/