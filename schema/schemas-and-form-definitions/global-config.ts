import {GeoPoint, options, UInt} from "../common/index";
import {sBoolean, sEnum, sInteger, sNumber, sObject, sString} from "json-schema-builder-ts/dist/types";
import {JsonSchema} from "json-schema-builder-ts";
import {rData} from "json-schema-builder-ts/dist/references";
import { RecomProperties } from "../common/recomsystems";

/**
export const paramGM = sObject({
        mapFile: sString,
        tempDirectory: sString
});

export const GraphManagerProperties = sObject({
    typeName = sEnum('GRAPH_HOPPER'),
    parameters: paramGM
});
*/

export const paramFleetManager = sObject({
        EventFile: sString
});

export const FleetManagerProperties = sObject({
    typeName = sEnum('FileBasedFleetManager'),
    parameters: paramFleetManager
});

export const paramDemandManager = sObject({
        demandDataFile: sString
});

export const DemandManagerProperties = sObject({
    typeName = sEnum('FileBasedDemandManager'),
    parameters: paramDemandManager
});

export const GlobalConfig = new JsonSchema(options, sObject({
    totalSimulationTime: UInt,
    reservationTime: sInteger().min(0).max(rData('1/totalSimulationTime')),
    randomSeed: sInteger().min(1),
    startDateTime: sString(),
    boundingBox: sObject({
        northWest: GeoPoint,
        southEast: GeoPoint,
    }).require.all().restrict(),
    debugMode: sBoolean(),
        
/**
    graphManagerType: GraphManagerProperties,
*/
    graphManagerType: sObject({
       typeName = sEnum('GRAPH_HOPPER'),
       parameters: sObject({
          mapFile: sString,
          tempDirectory: sString
       })
    }),
        
    recommendationSystemType: RecomProperties,
    fleetManagerType: FleetManagerProperties,
    demandManagerType: DemandManagerProperties

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
