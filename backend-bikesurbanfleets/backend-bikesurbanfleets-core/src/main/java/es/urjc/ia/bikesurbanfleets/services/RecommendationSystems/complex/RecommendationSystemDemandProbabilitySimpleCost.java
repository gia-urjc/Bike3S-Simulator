/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.PastRecommendations;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationData;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 *
 * @author holger
 */
@RecommendationSystemType("SIMPLECOST")
public class RecommendationSystemDemandProbabilitySimpleCost extends AbstractRecommendationSystemDemandProbabilityBased {

    public static class RecommendationParameters extends AbstractRecommendationSystemDemandProbabilityBased.RecommendationParameters {

        private double desireableProbability = 0.8;
        private double unsucesscostRentPenalisation = 6000; //with calculator2bis=between 4000 and 6000
        private double unsucesscostReturnPenalisation = 6000; //with calculator2bis=between 4000 and 6000
    }

    private RecommendationParameters parameters;
    private CostCalculatorSimple scc;

    public RecommendationSystemDemandProbabilitySimpleCost(JsonObject recomenderdef, SimulationServices ss) throws Exception {
        //***********Parameter treatment*****************************
        //parameters are read in the superclass
        //afterwards, they have to be cast to this parameters class
        super(recomenderdef, ss, new RecommendationParameters());
        this.parameters = (RecommendationParameters) (super.parameters);
        scc = new CostCalculatorSimple(
                parameters.unsucesscostRentPenalisation,
                parameters.unsucesscostReturnPenalisation);
    }

    @Override
    protected Stream<StationData> specificOrderStationsRent(Stream<StationData> stationdata, List<Station> allstations, GeoPoint currentuserposition, double maxdistance) {
        return stationdata
      //           .filter(expectedBikes())

                .map(sd -> {
                    double cost = scc.calculateCostRentSimple(sd, sd.probabilityTake, sd.walktime);
                    sd.individualCost = cost;
                    sd.totalCost = cost;
                    return sd;
                })//apply function to calculate cost 
                .sorted(costRentComparator(parameters.desireableProbability));
    }

    @Override
    protected Stream<StationData> specificOrderStationsReturn(Stream<StationData> stationdata, List<Station> allstations, GeoPoint currentuserposition, GeoPoint userdestination) {
        return stationdata
   //                             .filter(expectedSlots())

                .map(sd -> {
                    double cost = scc.calculateCostReturnSimple(sd, sd.probabilityReturn, sd.biketime, sd.walktime);
                    sd.individualCost = cost;
                    sd.totalCost = cost;
                    return sd;
                })//apply function to calculate cost
                .sorted(costReturnComparator(parameters.desireableProbability));
    }
    
        private Predicate<StationData> expectedBikes() {
        return (StationData sd) -> {
            PastRecommendations.ExpBikeChangeResult er = this.pastRecomendations.getExpectedBikechanges(sd.station.getId(), 0, sd.walktime);
            sd.expectedbikesAtArrival = sd.station.availableBikes() + er.changes + er.minpostchanges;
            return sd.expectedbikesAtArrival > 0;
        };
    }

    private Predicate<StationData> expectedSlots() {
        return (StationData sd) -> {
            PastRecommendations.ExpBikeChangeResult er = this.pastRecomendations.getExpectedBikechanges(sd.station.getId(), 0, sd.biketime);
            sd.expectedslotsAtArrival = sd.station.availableSlots() - er.changes - er.maxpostchanges;
            return sd.expectedslotsAtArrival > 0;
        };
    }

}
