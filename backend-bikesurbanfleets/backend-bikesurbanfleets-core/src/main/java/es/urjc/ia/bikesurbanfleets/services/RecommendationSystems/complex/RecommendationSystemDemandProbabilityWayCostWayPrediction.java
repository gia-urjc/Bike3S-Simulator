package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationData;
import static es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex.AbstractRecommendationSystemDemandProbabilityBased.costRentComparator;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex.ComplexCostCalculatorNew.stationPoint;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex.FutureCostCalculatorNew.ddPair;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import java.util.ArrayList;
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
@RecommendationSystemType("WAYCOST_WAYPREDICTION")
public class RecommendationSystemDemandProbabilityWayCostWayPrediction extends AbstractRecommendationSystemDemandProbabilityBased {

    public static class RecommendationParameters extends AbstractRecommendationSystemDemandProbabilityBased.RecommendationParameters {

        //parameters for best station
        private double minimumMarginProbability = 0.0001;
        private double minProbBestNeighbourRecommendation = 0;
        private double desireableProbability = 0.8;
        private double unsucesscostRentPenalisation = 6000;
        private double unsucesscostReturnPenalisation = 6000;
        private int maxNeighbours = 30;

        //prediction parameters
        private double predictionunsucessCostRent=3000;
        private double predictionunsucessCostReturn=3000;
        private int predictionWindow = 900;
        private double predictionMultiplier = 0.5;
    }

    private RecommendationParameters parameters;
    private ComplexCostCalculatorNew ucc;
    private FutureCostCalculatorNew fcc;

    public RecommendationSystemDemandProbabilityWayCostWayPrediction(JsonObject recomenderdef, SimulationServices ss) throws Exception {
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
                probutils, 
                graphManager);
        fcc = new FutureCostCalculatorNew(
                parameters.predictionunsucessCostRent,
                parameters.predictionunsucessCostReturn,
                probutils,
                parameters.expectedWalkingVelocity,
                parameters.expectedCyclingVelocity,
                graphManager);
    }

    @Override
    protected Stream<StationData> specificOrderStationsRent(Stream<StationData> stationdata, List<Station> allstations, GeoPoint currentuserposition, double maxdistance) {
        return stationdata
                .map(sd -> {
                    List<stationPoint> way = new LinkedList<stationPoint>();
                    sd.individualCost = ucc.calculateWayCostRentHeuristic(way, sd, sd.walkdist, new ArrayList<>(), allstations, sd.probabilityTake, 0, maxdistance, true, parameters.maxNeighbours);
                    ddPair futurecost
                            =fcc.calculateFurtureCostChangeTakeExpectedFailsWay( way, this.parameters.predictionWindow, allstations);
                    sd.takecostdiff = futurecost.takecostdiff;
                    sd.returncostdiff = futurecost.returncostdiff;
                    sd.totalCost = sd.individualCost + this.parameters.predictionMultiplier * (sd.takecostdiff + sd.returncostdiff);
                    return sd;
                })//apply function to calculate cost 
                .sorted(costRentComparator(parameters.desireableProbability));
    }

    @Override
    protected Stream<StationData> specificOrderStationsReturn(Stream<StationData> stationdata, List<Station> allstations, GeoPoint currentuserposition, GeoPoint userdestination) {
        return stationdata
                .map(sd -> {
                    List<stationPoint> way = new LinkedList<stationPoint>();
                    sd.individualCost = ucc.calculateWayCostReturnHeuristic(way, sd, sd.bikedist, userdestination, new ArrayList<>(), allstations, sd.probabilityReturn, 0, true, parameters.maxNeighbours);
                    ddPair futurecost
                            =fcc.calculateFurtureCostChangeReturnExpectedFailsWay( way, this.parameters.predictionWindow, allstations);
                    sd.takecostdiff = futurecost.takecostdiff;
                    sd.returncostdiff = futurecost.returncostdiff;
                    sd.totalCost = sd.individualCost + this.parameters.predictionMultiplier * (sd.takecostdiff + sd.returncostdiff);
                    return sd;
                })//apply function to calculate cost
                .sorted(costReturnComparator(parameters.desireableProbability));
    }

}
