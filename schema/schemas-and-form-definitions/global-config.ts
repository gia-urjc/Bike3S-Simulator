import {GeoPoint, options, UInt} from "../common/index";
import {sBoolean, sEnum, sInteger, sNumber, sObject, sString} from "json-schema-builder-ts/dist/types";
import {JsonSchema} from "json-schema-builder-ts";
import {rData} from "json-schema-builder-ts/dist/references";

export const GlobalConfig = new JsonSchema(options, sObject({
    totalSimulationTime: UInt,
    reservationTime: sInteger().min(0).max(rData('1/totalSimulationTime')),
    randomSeed: sInteger().min(1),
    recommendationSystemType: sEnum('AVAILABLE_RESOURCES_RATIO'),
    graphManagerType: sEnum('GRAPH_HOPPER'),
    maxDistanceRecommendation: sNumber().min(0),
    boundingBox: sObject({
        northWest: GeoPoint,
        southEast: GeoPoint,
    }).require.all().restrict(),
    debugMode: sBoolean()
}).require('totalSimulationTime', 'reservationTime', 'boundingBox', 'recommendationSystemType', 'graphManagerType', 'maxDistanceRecommendation'));

export const layout = 
[
    {key: "totalSimulationTime", placeholder: "Total simulation time in seconds."},
    {key: "reservationTime", placeholder: "Maximum time for reserves in seconds."},
    {key: "randomSeed", placeholder: "Random Seed"},
    {key: "recommendationSystemType", placeholder: "Type of the Recommendation System"},
    {key: "graphManagerType", placeholder: "Type of graph Manager"},
    {key: "maxDistanceRecommendation", placeholder: "Max distance recommended by the system"},
    {key: "boundingBox", htmlClass: "bold-text"},
    {key: "boundingBox.northWest.latitude", placeholder: "Nort-West latitude", title: "North latitude"},
    {key: "boundingBox.northWest.longitude", placeholder: "Nort-West longitude", title: "North longitude"},
    {key: "boundingBox.southEast.latitude", placeholder: "South-West latitude", title: "South latitude"},
    {key: "boundingBox.southEast.longitude", placeholder: "South-West longitude", title: "South longitude"}
];