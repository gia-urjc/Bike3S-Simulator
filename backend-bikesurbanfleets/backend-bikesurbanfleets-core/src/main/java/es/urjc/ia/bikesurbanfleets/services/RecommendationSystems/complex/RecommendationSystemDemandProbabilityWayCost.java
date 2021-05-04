package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationData;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex.ComplexCostCalculatorNew.stationPoint;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import java.util.ArrayList;

import java.util.Comparator;
import java.util.LinkedList;
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
@RecommendationSystemType("WAYCOST")
public class RecommendationSystemDemandProbabilityWayCost extends AbstractRecommendationSystemDemandProbabilityBased {

    public static class RecommendationParameters extends AbstractRecommendationSystemDemandProbabilityBased.RecommendationParameters {

        private double minimumMarginProbability = 0.0001;
        private double minProbBestNeighbourRecommendation = 0;
        private double desireableProbability = 0.1;
        private double unsucesscostRentPenalisation = 3000; //with calculator2bis=between 4000 and 6000
        private double unsucesscostReturnPenalisation = 30000; //with calculator2bis=between 4000 and 6000
        private int maxNeighbours=30;
    }

    private RecommendationParameters parameters;
    private ComplexCostCalculatorNew ucc;

    public RecommendationSystemDemandProbabilityWayCost(JsonObject recomenderdef, SimulationServices ss) throws Exception {
        //***********Parameter treatment*****************************
        //parameters are read in the superclass
        //afterwards, they have to be cast to this parameters class
        super(recomenderdef, ss, new RecommendationParameters());
        this.parameters = (RecommendationParameters) (super.parameters);

        ucc = new ComplexCostCalculatorNew(parameters.minimumMarginProbability, 
                parameters.unsucesscostRentPenalisation,
                parameters.unsucesscostReturnPenalisation,
                parameters.expectedWalkingVelocity,
                parameters.expectedCyclingVelocity, 
                parameters.minProbBestNeighbourRecommendation,
                probutils, graphManager);
    }

    @Override
    protected Stream<StationData> specificOrderStationsRent(Stream<StationData> stationdata, List<Station> allstations, GeoPoint currentuserposition, double maxdistance) {
        return stationdata
                .map(sd -> {
                    sd.totalCost = ucc.calculateWayCostRentHeuristic(new LinkedList<>(), sd, sd.walkdist, new ArrayList<>(), allstations, sd.probabilityTake, 0, maxdistance, true, parameters.maxNeighbours);
                    return sd;
                })//apply function to calculate cost 
                .sorted(costRentComparator(parameters.desireableProbability));
    }

    @Override
    protected Stream<StationData> specificOrderStationsReturn(Stream<StationData> stationdata, List<Station> allstations, GeoPoint currentuserposition, GeoPoint userdestination) {
        return stationdata
                .map(sd -> {
                    sd.totalCost = ucc.calculateWayCostReturnHeuristic(new LinkedList<>(), sd, sd.bikedist, userdestination,new ArrayList<>(), allstations, sd.probabilityReturn, 0, true, parameters.maxNeighbours);
                    return sd;
                })//apply function to calculate cost
                .sorted(costReturnComparator(parameters.desireableProbability));
    }
}
