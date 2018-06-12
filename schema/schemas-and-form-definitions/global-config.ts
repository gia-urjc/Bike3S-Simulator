import {GeoPoint, options, UInt} from "../common/index";
import {sBoolean, sEnum, sInteger, sNumber, sObject, sString} from "json-schema-builder-ts/dist/types";
import {JsonSchema} from "json-schema-builder-ts";
import {rData} from "json-schema-builder-ts/dist/references";

export const GlobalConfig = new JsonSchema(options, sObject({
    totalSimulationTime: UInt,
    debugMode: sBoolean(),
    reservationTime: sInteger().min(0).max(rData('1/totalSimulationTime')),
    randomSeed: sInteger().min(1),
    boundingBox: sObject({
        northWest: GeoPoint,
        southEast: GeoPoint,
    }).require.all().restrict(),
    map: sString().pattern(/\.osm/),
    recommendationSystemType: sEnum('AVAILABLE_RESOURCES_RATIO'),
    graphManagerType: sEnum('GRAPH_HOPPER'),
    maxDistanceRecommendation: sNumber().min(0)
}).require('totalSimulationTime', 'reservationTime', 'boundingBox', 'map', 'recommendationSystemType', 'graphManagerType', 'maxDistanceRecommendation'));