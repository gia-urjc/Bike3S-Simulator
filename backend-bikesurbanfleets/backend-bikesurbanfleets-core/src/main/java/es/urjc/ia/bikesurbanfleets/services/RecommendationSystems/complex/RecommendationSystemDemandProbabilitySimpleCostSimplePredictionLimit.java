package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationData;
import static es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex.AbstractRecommendationSystemDemandProbabilityBased.costRentComparator;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex.FutureCostCalculatorNew.ddPair;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
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
@RecommendationSystemType("SIMPLECOST_SIMPLEPREDICTION_LIMIT")
public class RecommendationSystemDemandProbabilitySimpleCostSimplePredictionLimit extends AbstractRecommendationSystemDemandProbabilityBased {

    
    public static class RecommendationParameters extends AbstractRecommendationSystemDemandProbabilityBased.RecommendationParameters {

        private double desireableProbability = 0.8;
        private double unsucesscostRentPenalisation = 6000; //with calculator2bis=between 4000 and 6000
        private double unsucesscostReturnPenalisation = 6000; //with calculator2bis=between 4000 and 6000

        private double predictionunsucessCostRent=3000;
        private double predictionunsucessCostReturn=3000;
        private int predictionWindow = 900;
        private double predictionMultiplier = 0.5;
        private double MaxExtraTime = 300;
    }
    private RecommendationParameters parameters;
    private CostCalculatorSimple scc;
    private FutureCostCalculatorNew fcc;

    private int counternullrent=0;
    private int counternullreturn=0;
    private int counterbestindividualrent=0;
    private int counterbestindividualreturn=0;
    private int counterbesttotalreturn=0;
    private int counterbesttotalrent=0;
    private int counterbestindividualInsteadOftotalreturn=0;
    private int counterbestindividualInsteadOftotalrent=0;
    private int totalreturn=0;
    private int totalrent=0;        
    
    public RecommendationSystemDemandProbabilitySimpleCostSimplePredictionLimit(JsonObject recomenderdef, SimulationServices ss) throws Exception {
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
        totalrent++;
        Stream<StationData> result;
        List<StationData> ind= stationdata
                .map(sd -> {
                    sd.individualCost = scc.calculateCostRentSimple(sd, sd.probabilityTake, sd.walktime);
                    ddPair futurecost = 
                            fcc.calculateFurtureCostChangeTakeExpectedFails(sd.station, sd.probabilityTake,sd.walktime, this.parameters.predictionWindow, allstations);
                    sd.takecostdiff=futurecost.takecostdiff;
                    sd.returncostdiff=futurecost.returncostdiff;
                    sd.totalCost = sd.individualCost+this.parameters.predictionMultiplier*(sd.takecostdiff+sd.returncostdiff);
                    return sd;
                })//apply function to calculate cost 
                .sorted(costRentComparator(parameters.desireableProbability)).collect(Collectors.toList());
        if (!ind.isEmpty()){
            StationData bestglobal=ind.get(0);
            StationData bestindividual=bestglobal;
            boolean besttotal=false;
            for (StationData s: ind){
                if (s.individualCost<bestindividual.individualCost){
                    bestindividual=s;
                    besttotal=true;
                }
            }
            if (besttotal && bestindividual.walktime+this.parameters.MaxExtraTime<bestglobal.walktime) {
                counterbestindividualInsteadOftotalrent++;
                 System.out.println("take: different globally best station/ ind. best  too far not taken: "+bestglobal.station.getId()+ "/" +
                         bestindividual.station.getId() + " time diff: " + (bestglobal.walktime-bestindividual.walktime));
            } else if (besttotal) {
                 System.out.println("take: different globally best station/ ind. best taken: "+bestglobal.station.getId()+ "/" +
                         bestindividual.station.getId() + " time diff: " + (bestglobal.walktime-bestindividual.walktime));
                counterbesttotalrent++; 
            } else {
                counterbestindividualrent++;
                 System.out.println("take: sameglobally best station: "+bestglobal.station.getId()+ "/" +
                         bestindividual.station.getId() + " time diff: " + (bestglobal.walktime-bestindividual.walktime));
            }  
            result=ind.stream().sorted(modifiedCostRentComparator(bestindividual.walktime+this.parameters.MaxExtraTime,
                    bestglobal.individualCost));
        } else { 
            counternullrent++;
            result=ind.stream();
        };
        System.out.println("takes total/null/best individual/best global/individual instead of global: "+
                totalrent+ "/"+ counternullrent + "/"+
                counterbestindividualrent+ "/"+ counterbesttotalrent+ "/"+
                counterbestindividualInsteadOftotalrent);
        return result;
    }
 
    @Override
    protected Stream<StationData> specificOrderStationsReturn(Stream<StationData> stationdata, List<Station> allstations, GeoPoint currentuserposition, GeoPoint userdestination) {
        totalreturn++;
        Stream<StationData> result;
        List<StationData> ind= stationdata
                .map(sd -> {
                    sd.individualCost = scc.calculateCostReturnSimple(sd, sd.probabilityReturn, sd.biketime, sd.walktime);
                    ddPair futurecost = 
                            fcc.calculateFurtureCostChangeReturnExpectedFails(sd.station, sd.probabilityReturn,sd.biketime, this.parameters.predictionWindow, allstations);
                    sd.takecostdiff=futurecost.takecostdiff;
                    sd.returncostdiff=futurecost.returncostdiff;
                    sd.totalCost = sd.individualCost+this.parameters.predictionMultiplier*(sd.takecostdiff+sd.returncostdiff);
                    return sd;
                })//apply function to calculate cost
                .sorted(costReturnComparator(parameters.desireableProbability)).collect(Collectors.toList());
        if (!ind.isEmpty()){
            StationData bestglobal=ind.get(0);
            StationData bestindividual=bestglobal;
            boolean besttotal=false;
            for (StationData s: ind){
                if (s.individualCost<bestindividual.individualCost){
                    bestindividual=s;
                    besttotal=true;
                }
            }
            if (besttotal && (bestindividual.walktime+ bestindividual.biketime+this.parameters.MaxExtraTime)<(bestglobal.walktime+bestglobal.biketime)){
                counterbestindividualInsteadOftotalreturn++;
                 System.out.println("return: different globally best station/ ind. best  too far not taken: "+bestglobal.station.getId()+ "/" +
                       bestindividual.station.getId() + " time diff: " + (bestglobal.walktime+bestglobal.biketime-bestindividual.walktime - bestindividual.biketime));
            } else if (besttotal) {
                counterbesttotalreturn++; 
                 System.out.println("return: different globally best station/ ind. best  taken: "+bestglobal.station.getId()+ "/" +
                       bestindividual.station.getId() + " time diff: " + (bestglobal.walktime+bestglobal.biketime-bestindividual.walktime - bestindividual.biketime));
            } else {
                counterbestindividualreturn++;
                 System.out.println("return: same globally best station/ ind.: "+bestglobal.station.getId()+ "/" +
                       bestindividual.station.getId() + " time diff: " + (bestglobal.walktime+bestglobal.biketime-bestindividual.walktime - bestindividual.biketime));
            }
            result=ind.stream().sorted(modifiedCostReturnComparator(
                    bestindividual.walktime+bestindividual.biketime+this.parameters.MaxExtraTime,
                    bestglobal.individualCost));
        } else { 
            counternullreturn++;
            result=ind.stream();
        }
        System.out.println("returns total/null/best individual/best global/individual instead of global: "+
                totalreturn+ "/"+ counternullreturn + "/"+
                counterbestindividualreturn+ "/"+ counterbesttotalreturn+ "/"+
                counterbestindividualInsteadOftotalreturn);
        return result;
    }
    
        //comparators to be used by the recommenders
    protected static Comparator<StationData> individualCostComparator() {
        return (newSD, oldSD) -> {
            return Double.compare(newSD.individualCost, oldSD.individualCost);
        };
    }
        //comparators to be used by the recommenders
    protected static Comparator<StationData> modifiedCostRentComparator(double maxtime, double maxindcost) {
        return (newSD, oldSD) -> {
            boolean A=(newSD.individualCost<=maxindcost && newSD.walktime<=maxtime);
            boolean B=(oldSD.individualCost<=maxindcost && oldSD.walktime<=maxtime);
            if (A && B) return Double.compare(newSD.totalCost, oldSD.totalCost);        
            if (A) return -1;
            if (B) return +1;
            return Double.compare(newSD.totalCost, oldSD.totalCost); 
        };
    }
        //comparators to be used by the recommenders
    protected static Comparator<StationData> modifiedCostReturnComparator(double maxtime, double maxindcost) {
        return (newSD, oldSD) -> {
            boolean A=(newSD.individualCost<=maxindcost && (newSD.walktime+newSD.biketime)<=maxtime);
            boolean B=(oldSD.individualCost<=maxindcost && (oldSD.walktime+oldSD.biketime)<=maxtime);
            if (A && B) return Double.compare(newSD.totalCost, oldSD.totalCost);        
            if (A) return -1;
            if (B) return +1;
            return Double.compare(newSD.totalCost, oldSD.totalCost); 
        };
    }

}
