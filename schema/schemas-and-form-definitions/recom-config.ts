import { UInt } from "../common";
import { sNumber } from "json-schema-builder-ts/dist/types";


export const typeParameters = {
    AVAILABLE_RESOURCES: {
        maxDistanceRecommendation: UInt
    },
    RESOURCES_RATIO: {
        maxDistanceRecommendation: UInt,
    },
    AVAILABLE_RESOURCES_RATIO_PAPER: {
        maxDistanceRecommendation: UInt
    },
    DISTANCE_RESOURCES_RATIO: {
        maxDistanceRecommendation: UInt
    },
    SURROUNDING_STATIONS: {
        maxDistanceRecommendation: UInt
    },
    SURROUNDING_STATIONS_COMPLEX_INCENTIVES: {
        maxDistanceRecommendation: UInt,
        COMPENSATION: sNumber(),
        EXTRA: sNumber()
    },
    SURROUNDING_STATIONS_SIMPLE_INCENTIVES: {
        maxDistanceRecommendation: UInt,
        COMPENSATION: sNumber(),
        EXTRA: sNumber()
    },
    HOLGER_DistanceProbabilityRECOMENDER: {
        MAXDIFF : sNumber(),
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
    }
};