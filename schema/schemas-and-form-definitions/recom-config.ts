import { UInt } from "../common";
import { sNumber } from "json-schema-builder-ts/dist/types";


export const typeParameters = {
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
    SURROUNDING_AVAILABLE_RESOURCES: {
        maxDistanceRecommendation: UInt,
        MaxDistanceSurroundingStations: UInt
    },
    SURROUNDING_DISTANCE_RESOURCES: {
        maxDistanceRecommendation: UInt,
        MaxDistanceSurroundingStations: UInt
    },

    DEMAND_PROBABILITY: {
        maxDistanceRecommendation: UInt,
        upperProbabilityBound: UInt,
        desireableProbability: sNumber(),
        probfactor: sNumber()
    },
    DEMAND_cost: {
        maxDistanceRecommendation: UInt,
        minimumMarginProbability: sNumber(),
        minProbBestNeighbourRecommendation: sNumber(),
        desireableProbability: sNumber(),
        penalisationfactorrent: sNumber(),
        penalisationfactorreturn: sNumber(),
        maxStationsToReccomend: UInt,
        unsucesscostRent: sNumber(),
        unsucesscostReturn: sNumber(),
        MaxCostValue: sNumber()
    },
        DEMAND_cost_prediction: {
        maxDistanceRecommendation: UInt,
        minimumMarginProbability: sNumber(),
        minProbBestNeighbourRecommendation: sNumber(),
        desireableProbability: sNumber(),
        penalisationfactorrent: sNumber(),
        penalisationfactorreturn: sNumber(),
        maxStationsToReccomend: UInt,
        unsucesscostRent: sNumber(),
        unsucesscostReturn: sNumber(),
        MaxCostValue: sNumber(),
        PredictionNorm: UInt,
        predictionWindow: UInt,
        normmultiplier: sNumber()
    }

};
