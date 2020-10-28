/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.simple;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.PastRecommendations;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationData;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 *
 * @author holger
 */
@RecommendationSystemType("DISTANCE_EXPECTED_RESOURCES")
public class RecommendationSystemByDistanceExpectedResources extends RecommendationSystem {

    public static class RecommendationParameters extends RecommendationSystem.RecommendationParameters {
    }
    private RecommendationParameters parameters;

    public RecommendationSystemByDistanceExpectedResources(JsonObject recomenderdef, SimulationServices ss) throws Exception {
        //***********Parameter treatment*****************************
        //parameters are read in the superclass
        //afterwards, they have to be cast to this parameters class
        super(recomenderdef, ss, new RecommendationParameters());
        this.parameters = (RecommendationParameters) (super.parameters);
    }

    public Stream<StationData> recommendStationToRentBike(final Stream<StationData> candidates, final GeoPoint point, double maxdist) {
        return candidates
                .filter(expectedBikes())
                .sorted(byProportionBetweenDistanceAndBikes());
    }

    public Stream<StationData> recommendStationToReturnBike(final Stream<StationData> candidates, final GeoPoint currentposition, final GeoPoint destination) {
        return candidates
                .filter(expectedSlots())
                .sorted(byProportionBetweenDistanceAndSlots());
    }

    private Comparator<StationData> byProportionBetweenDistanceAndBikes() {
        return (s1, s2) -> {
            double aux1 = (double)s1.expectedbikesAtArrival/(double)s1.station.getCapacity();
            double aux2 = (double)s2.expectedbikesAtArrival/(double)s2.station.getCapacity();
     /*        return Double.compare(
                    s1.walkdist / aux1,
                    s2.walkdist / aux2);
      */       return Double.compare(
                    s1.walkdist / s1.expectedbikesAtArrival ,
                    s2.walkdist / s2.expectedbikesAtArrival );
    //                s1.walkdist + (1-aux1) * 1200 ,
    //                s2.walkdist + (1-aux2) * 1200 );
        };
    }

    private Comparator<StationData> byProportionBetweenDistanceAndSlots() {
        return (s1, s2) -> {
            double aux1 = (double)s1.expectedslotsAtArrival/(double)s1.station.getCapacity();
            double aux2 = (double)s2.expectedslotsAtArrival/(double)s2.station.getCapacity();
   /*         return Double.compare(
                    s1.walkdist / aux1,
                    s2.walkdist / aux2);
    */        return Double.compare(
                     s1.walkdist / s1.expectedslotsAtArrival ,
                    s2.walkdist / s2.expectedslotsAtArrival );
 //                  s1.walkdist + (1-aux1) * 1200 ,
 //                   s2.walkdist + (1-aux2) * 1200 );
        } ;
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

    @Override
    protected void printRecomendationDetails(List<StationData> su, boolean rentbike, long maxnumber) {
        if (rentbike) {
            System.out.println("             id av ca expb    wtime");
            int i = 1;
            for (StationData s : su) {
                if (i > maxnumber) {
                    break;
                }
                System.out.format("%-3d Station %3d %2d %2d %2d   %8.2f %n",
                        i,
                        s.station.getId(),
                        s.station.availableBikes(),
                        s.station.getCapacity(),
                        (int) Math.round(s.expectedbikesAtArrival),
                        s.walktime);
                i++;
            }
        } else {
            System.out.println("             id av ca exps    wtime    btime");
            int i = 1;
            for (StationData s : su) {
                if (i > maxnumber) {
                    break;
                }
                System.out.format("%-3d Station %3d %2d %2d %2d   %8.2f %8.2f %n",
                        i,
                        s.station.getId(),
                        s.station.availableBikes(),
                        s.station.getCapacity(),
                        (int) Math.round(s.expectedslotsAtArrival),
                        s.walktime,
                        s.biketime);
                i++;
            }
        }
    }

}
