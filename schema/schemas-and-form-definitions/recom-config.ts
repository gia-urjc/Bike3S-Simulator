import { UInt } from "../common";
import { sNumber } from "json-schema-builder-ts/dist/types";


export const typeParameters = {
    FULLCAPRECOMENDER: {
        MAXDIFF: sNumber(),
        MAXTOTALDIST: sNumber()
    },
    HOLGER_FixedDistanceRECOMENDER: {
        MAXDIFF: sNumber(),
        MAXTOTALDIST: sNumber()
    },
    HOLGERRECOMENDER: {
        MAXDIFF: sNumber(),
        MAXTOTALDIST: sNumber()
    },
    HOLGERRECOMENDER_RANDOM: {
        MAXDIFF: sNumber(),
        MAXTOTALDIST: sNumber()
    },
    HOLGERRECOMENDER_SURROUNDING: {
        MAXDIFF: sNumber(),
        MAXTOTALDIST: sNumber(),
        MaxDistanceSurroundingStations: sNumber()
    },
    AVAILABLE_RESOURCES: {
        maxDistanceRecommendation: UInt
    },
    DISTANCE_RESOURCES: {
        maxDistanceRecommendation: UInt
    },
    DISTANCE_RATIO: {
        maxDistanceRecommendation: UInt
    },
    RESOURCES_RATIO: {
        maxDistanceRecommendation: UInt
    },
    SURROUNDING_STATIONS: {
        maxDistanceRecommendation: UInt
    },
    SURROUNDING_STATIONS_COMPLEX_INCENTIVES: {
        maxDistanceRecommendation: UInt
    },
    SURROUNDING_STATIONS_SIMPLE_INCENTIVES: {
        maxDistanceRecommendation: UInt,
    },
    SURROUNDING_AVAILABLE_RESOURCES: {
        maxDistanceRecommendation: UInt,
        MaxDistanceSurroundingStations: UInt
    },
    SURROUNDING_DISTANCE_RESOURCES: {
        maxDistanceRecommendation: UInt,
        MaxDistanceSurroundingStations: UInt
    }
};