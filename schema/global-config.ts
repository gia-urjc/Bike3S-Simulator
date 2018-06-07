import {GeoPoint, options, UInt} from "./common";
import {sBoolean, sEnum, sInteger, sNumber, sObject, sString} from "json-schema-builder-ts/dist/types";
import {JsonSchema} from "json-schema-builder-ts";
import {rData} from "json-schema-builder-ts/dist/references";

export default new JsonSchema(options, sObject({
    totalSimulationTime: UInt,
    debugMode: sBoolean(),
    reservationTime: sInteger().min(0).max(rData('1/totalSimulationTime')),
    randomSeed: sInteger(),
    boundingBox: sObject({
        northWest: GeoPoint,
        southEast: GeoPoint,
    }).require.all().restrict(),
    map: sString().pattern(/\.osm/),
    recommendationSystemType: sEnum('AVAILABLE_RESOURCES_RATIO'),
    graphManagerType: sEnum('GRAPH_HOPPER'),
    maxDistanceRecommendation: sNumber().min(0)
}).require.all().restrict());