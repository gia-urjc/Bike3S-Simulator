import {GeoPoint, options, UInt} from "../common/index";
import {sBoolean, sEnum, sInteger, sNumber, sObject, sString} from "json-schema-builder-ts/dist/types";
import {JsonSchema} from "json-schema-builder-ts";
import {rData} from "json-schema-builder-ts/dist/references";
import { RecomProperties } from "../common/recomsystems";

export const GlobalConfig = new JsonSchema(options, sObject({
    totalSimulationTime: UInt,
    reservationTime: sInteger().min(0).max(rData('1/totalSimulationTime')),
    randomSeed: sInteger().min(1),
    recommendationSystemType: RecomProperties,
    graphManagerType: sEnum('GRAPH_HOPPER'),
    boundingBox: sObject({
        northWest: GeoPoint,
        southEast: GeoPoint,
    }).require.all().restrict(),
    debugMode: sBoolean(),
    loadDemandData: sBoolean(),
    startDateTime: sString(),
}).require('totalSimulationTime', 'reservationTime', 'boundingBox', 'graphManagerType'));

export const layout = 
[
    {key: "totalSimulationTime", placeholder: "Total simulation time in seconds."},
    {key: "reservationTime", placeholder: "Maximum time for reserves in seconds."},
    {key: "randomSeed", placeholder: "Random Seed"},
    {key: "graphManagerType", placeholder: "Type of graph Manager"},
    {key: "debugMode", placeholder: "If checked, log files will be generated"},
    {key: "loadDemandData", placeholder: "Specifies whether or not demand data should be loaded (false if not specified)"},
    {key: "startDateTime", placeholder: "Specifies the day and start time of the simulation (will not be used if not set)"}
];
