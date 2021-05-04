package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationData;
import static es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex.AbstractRecommendationSystemDemandProbabilityBased.costRentComparator;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex.FutureCostCalculatorNew.ddPair;
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
@RecommendationSystemType("SIMPLECOST_SURROUNDINGPREDICTION")
public class RecommendationSystemDemandProbabilitySimpleCostSurroundingPrediction extends AbstractRecommendationSystemDemandProbabilityBased {

    public static class RecommendationParameters extends AbstractRecommendationSystemDemandProbabilityBased.RecommendationParameters {

        private double desireableProbability = 0.8;
        private double unsucesscostRentPenalisation = 6000; //with calculator2bis=between 4000 and 6000
        private double unsucesscostReturnPenalisation = 6000; //with calculator2bis=between 4000 and 6000

        private double MaxDistanceSurroundingStations = 500;        
        private double predictionunsucessCostRent=3000;
        private double predictionunsucessCostReturn=3000;
        private int predictionWindow = 900;
        private double predictionMultiplier = 0.5;
    }
    private RecommendationParameters parameters;
    private CostCalculatorSimple scc;
    private FutureCostCalculatorNew fcc;

    public RecommendationSystemDemandProbabilitySimpleCostSurroundingPrediction(JsonObject recomenderdef, SimulationServices ss) throws Exception {
        //***********Parameter treatment*****************************
        //parameters are read in the superclass
        //afterwards, they have to be cast to this parameters class
        super(recomenderdef, ss, new RecommendationParameters());
        this.parameters = (RecommendationParameters) (super.parameters);
        scc = new CostCalculatorSimple(
                parameters.unsucesscostRentPenalisation,
                parameters.unsucesscostReturnPenalisation);
        fcc= new FutureCostCalculatorNew(
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
                    sd.individualCost = scc.calculateCostRentSimple(sd, sd.probabilityTake, sd.walktime);
                    ddPair futurecost = fcc.calculateFurtureCostChangeTakeExpectedFailsSurrounding(
                            sd.station, sd.probabilityTake,sd.walktime, this.parameters.predictionWindow, allstations, this.parameters.MaxDistanceSurroundingStations);
                    sd.takecostdiff=futurecost.takecostdiff;
                    sd.returncostdiff=futurecost.returncostdiff;
                    sd.totalCost = sd.individualCost+this.parameters.predictionMultiplier*(sd.takecostdiff+sd.returncostdiff);
                    return sd;
                })//apply function to calculate cost 
                .sorted(costRentComparator(parameters.desireableProbability));
    }

    @Override
    protected Stream<StationData> specificOrderStationsReturn(Stream<StationData> stationdata, List<Station> allstations, GeoPoint currentuserposition, GeoPoint userdestination) {
        return stationdata
                .map(sd -> {
                    sd.individualCost = scc.calculateCostReturnSimple(sd, sd.probabilityReturn, sd.biketime, sd.walktime);
                    ddPair futurecost = fcc.calculateFurtureCostChangeReturnExpectedFailsSurrounding(
                            sd.station, sd.probabilityReturn,sd.biketime, this.parameters.predictionWindow, allstations, this.parameters.MaxDistanceSurroundingStations);
                    sd.takecostdiff=futurecost.takecostdiff;
                    sd.returncostdiff=futurecost.returncostdiff;
                    sd.totalCost = sd.individualCost+this.parameters.predictionMultiplier*(sd.takecostdiff+sd.returncostdiff);
                    return sd;
                })//apply function to calculate cost
                .sorted(costReturnComparator(parameters.desireableProbability));
    }
}
