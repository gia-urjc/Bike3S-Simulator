/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.simple;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationData;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 *
 * @author holger
 */
@RecommendationSystemType("SURROUNDING_AVAILABLE_RESOURCES")
public class RecommendationSystemSurroundingByAvailableResources extends RecommendationSystem {

    public static class RecommendationParameters extends RecommendationSystem.RecommendationParameters {

        /**
         * It is the maximum distance in meters between the recommended stations
         * and the indicated geographical point.
         */
        private int maxDistanceRecommendationReturn = 600;

        /**
         * It is the maximum distance in meters between a station and the
         * stations we take into account for checking the area
         */
        private double MaxDistanceSurroundingStations = 500;
    }

    private RecommendationParameters parameters;

    public RecommendationSystemSurroundingByAvailableResources(JsonObject recomenderdef, SimulationServices ss) throws Exception {
        //***********Parameter treatment*****************************
        //parameters are read in the superclass
        //afterwards, they have to be cast to this parameters class
        super(recomenderdef, ss, new RecommendationParameters());
        this.parameters = (RecommendationParameters) (super.parameters);
    }

    @Override
    public Stream<StationData> recommendStationToRentBike(final Stream<StationData> candidates, final GeoPoint point, double maxdist) {
        return candidates
                .filter(s -> s.availableBikes > 0) //filter station candat¡dates
                .map(s -> {
                    getStationQuality(s, true);
                    return s;
                })//apply function to calculate utilities the utility data
                .sorted(byQualityRenting()); //sort by utility
    }

    public Stream<StationData> recommendStationToReturnBike(final Stream<StationData> candidates, final GeoPoint currentposition, final GeoPoint destination) {
        return candidates
                .filter(s -> s.availableSlots > 0) //filter station candat¡dates
                .map(s -> {
                    getStationQuality(s, false);
                    return s;
                })//apply function to calculate utilities the utility data
                .sorted(byQualityReturning()); //sort by utility
    }

    private void getStationQuality(StationData s, boolean take) {
        double summation = 0;
        double factor, multiplication;
        double nearestdist = Double.MAX_VALUE;
        Station nearest = null;
        for (Station other : stationManager.consultStations()) {
            double dist = s.station.getPosition().eucleadeanDistanceTo(other.getPosition());
            if (dist <= parameters.MaxDistanceSurroundingStations) {
                factor = (parameters.MaxDistanceSurroundingStations - dist) / parameters.MaxDistanceSurroundingStations;
                multiplication = take ? (other.availableBikes() * factor) : (other.availableSlots() * factor);
                summation += multiplication;
                if (dist < nearestdist && other.getId() != s.station.getId()) {
                    nearest = other;
                    nearestdist = dist;
                }
            }
        }
        s.quality = summation;
        s.nearest = nearest;
        s.nearestDistance = nearestdist;
    }

    public Comparator<StationData> byQualityRenting() {
        return (s1, s2) -> {
            int i = Double.compare(s1.quality, s2.quality);
            if (i < 0) {
                return +1;
            }
            if (i > 0) {
                return -1;
            }
            return Double.compare(
                    s1.walkdist, s2.walkdist);
        };
    }

    private Comparator<StationData> byQualityReturning() {
        return (s1, s2) -> {
            if (s1.walkdist <= this.parameters.maxDistanceRecommendationReturn
                    && s2.walkdist > this.parameters.maxDistanceRecommendationReturn) {
                return -1;
            } else if (s1.walkdist > this.parameters.maxDistanceRecommendationReturn
                    && s2.walkdist <= this.parameters.maxDistanceRecommendationReturn) {
                return +1;
            } else if (s1.walkdist > this.parameters.maxDistanceRecommendationReturn
                    && s2.walkdist > this.parameters.maxDistanceRecommendationReturn) {
                return Double.compare(
                        s1.walkdist, s2.walkdist);
            } else {
                int i = Double.compare(s1.quality, s2.quality);
                if (i < 0) {
                    return +1;
                }
                if (i > 0) {
                    return -1;
                }
                return Double.compare(
                        s1.walkdist, s2.walkdist);
            }
        };
    }

    @Override
    protected void printRecomendationDetails(List<StationData> su, boolean rentbike, long maxnumber) {
        staticPrintRecomendationDetails(su, rentbike, maxnumber);
    }

    protected static void staticPrintRecomendationDetails(List<StationData> su, boolean rentbike, long maxnumber) {
        System.out.println("             id av ca    wtime  quality nneig nndist nnav");
        int i = 1;
        for (StationData s : su) {
            if (i > maxnumber) {
                break;
            }
            if (s.nearest != null) {
                System.out.format("%-3d Station %3d %2d %2d %8.2f %8.2f %3d %8.2f %2d %n",
                        i,
                        s.station.getId(),
                        s.station.availableBikes(),
                        s.station.getCapacity(),
                        s.walktime,
                        s.quality,
                        s.nearest.getId(),
                        s.nearestDistance,
                        s.nearest.availableBikes());
            } else {
                System.out.format("%-3d Station %3d %2d %2d %8.2f %8.2f%n",
                        i,
                        s.station.getId(),
                        s.station.availableBikes(),
                        s.station.getCapacity(),
                        s.walktime,
                        s.quality);
            }
            i++;
        }
    }
}
