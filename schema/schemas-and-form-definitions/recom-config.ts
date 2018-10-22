import { UInt } from "../common";
import { sNumber } from "json-schema-builder-ts/dist/types";


export const typeParameters = {
    HOLGERRECOMENDER: {
        MINCAP_TO_RECOMEND: UInt,
        MAXDIFF: UInt,
        MAXTOTALDIST: UInt
    },
    HOLGERRECOMENDER_RANDOM: {
        MINCAP_TO_RECOMEND: UInt,
        MAXDIFF: UInt,
        MAXTOTALDIST: UInt
    },
    AVAILABLE_RESOURCES: {
        maxDistanceRecommendation: UInt
    },
    AVAILABLE_RESOURCES_RATIO: {
        maxDistanceRecommendation: UInt,
        N_STATIONS: UInt
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
    }
};