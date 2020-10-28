package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationData;
import static es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex.AbstractRecommendationSystemDemandProbabilityBased.costRentComparator;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import java.util.List;
import java.util.stream.Stream;

/**
 * This class is a system which recommends the user the stations to which he
 * should go to contribute with system rebalancing. Then, this recommendation
 * system gives the user a list of stations ordered descending by the
 * "resources/capacityº" ratio.
 *
 * @author IAgroup
 *
 */
@RecommendationSystemType("DEMAND_cost_prediction_simple")
public class RecommendationSystemDemandProbabilityCostGlobalPredictionSimple extends AbstractRecommendationSystemDemandProbabilityBased {

    public static class RecommendationParameters extends AbstractRecommendationSystemDemandProbabilityBased.RecommendationParameters {

        private double desireableProbability = 0.8;
        private double MaxCostValue = 6000;
        private int PredictionNorm = 0;
        private int predictionWindow = 900;
        private double normmultiplier = 0.5;
    }
    private RecommendationParameters parameters;
    private CostCalculatorSimple2 scc;

    public RecommendationSystemDemandProbabilityCostGlobalPredictionSimple(JsonObject recomenderdef, SimulationServices ss) throws Exception {
        //***********Parameter treatment*****************************
        //parameters are read in the superclass
        //afterwards, they have to be cast to this parameters class
        super(recomenderdef, ss, new RecommendationParameters());
        this.parameters = (RecommendationParameters) (super.parameters);
        scc = new CostCalculatorSimple2(
                parameters.MaxCostValue,
                probutils, parameters.PredictionNorm, parameters.normmultiplier,
                parameters.expectedWalkingVelocity,
                parameters.expectedCyclingVelocity,
                graphManager);
    }

    @Override
    protected Stream<StationData> specificOrderStationsRent(Stream<StationData> stationdata, List<Station> allstations, GeoPoint currentuserposition, double maxdistance) {
        return stationdata
                .map(sd -> {
                    double cost = scc.calculateCostsRentAtStation(sd, allstations, this.parameters.predictionWindow);
                    sd.totalCost = cost;
                    sd.expectedTimeIfNotAbandon = sd.walktime;
                    sd.abandonProbability = (1 - sd.probabilityTake);
                    return sd;
                })//apply function to calculate cost 
                .sorted(costRentComparator(parameters.desireableProbability));
    }

    @Override
    protected Stream<StationData> specificOrderStationsReturn(Stream<StationData> stationdata, List<Station> allstations, GeoPoint currentuserposition, GeoPoint userdestination) {
        return stationdata
                .map(sd -> {
                    double cost = scc.calculateCostsReturnAtStation(sd, allstations, this.parameters.predictionWindow);
                    sd.totalCost = cost;
                    sd.expectedTimeIfNotAbandon = sd.walktime + sd.biketime;
                    sd.abandonProbability = (1 - sd.probabilityReturn);
                    return sd;
                })//apply function to calculate cost
                .sorted(costReturnComparator(parameters.desireableProbability));
    }
}
