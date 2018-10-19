package es.urjc.ia.bikesurbanfleets.consultSystems.recommendationSystemTypes;

import java.util.ArrayList;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.comparators.StationComparator;
import es.urjc.ia.bikesurbanfleets.consultSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.consultSystems.RecommendationSystemParameters;
import es.urjc.ia.bikesurbanfleets.consultSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.infraestructure.InfraestructureManager;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;

@RecommendationSystemType("HOLGERRECOMENDER")
public class HolgerRecomender extends RecommendationSystem {

    private class StationData {

        public Station station = null;
        double distance = 0.0;
        int bikes = 0;
        int capacity = 0;
        double utility = 0.0;
    }
    static public Comparator<StationData> byUtility() {
        return (s1, s2) -> Double.compare(s2.utility, s1.utility);
    }

    @RecommendationSystemParameters
    public class RecommendationParameters {

        /**
         * It is the maximum distance in meters between the recommended stations
         * and the indicated geographical point.
         */
        private int maxDistance = 800;

    }

    private RecommendationParameters parameters;

    /**
     * It contains several comparators to sort stations.
     */
    private StationComparator stationComparator;

    public HolgerRecomender(InfraestructureManager infraestructureManager,
            StationComparator stationComparator) {
        super(infraestructureManager);
        this.parameters = new RecommendationParameters();
        this.stationComparator = stationComparator;
    }

    public HolgerRecomender(InfraestructureManager infraestructureManager, StationComparator stationComparator,
            RecommendationParameters parameters) {
        super(infraestructureManager);
        this.parameters = parameters;
        this.stationComparator = stationComparator;
    }

    @Override
    public List<Recommendation> recommendStationToRentBike(GeoPoint point) {
        //get the list of closest stations
        // goes through the list
        // if a station has more than 40% of ratio recomend it
        List<Station> stations = validStationsToRentBike(infraestructureManager.consultStations());
        List<StationData> temp, stationdat;
        List<Recommendation> result;
        stationdat = calculateDataTake(stations, point);

        if (!stationdat.isEmpty()) {
            temp = stationdat.stream().sorted(byUtility()).collect(Collectors.toList());
            result = temp.stream().map(s -> new Recommendation(s.station, 0.0)).collect(Collectors.toList());
        } else {
            throw new RuntimeException("no recomended station");
        }
        return result;
    }

    @Override
    public List<Recommendation> recommendStationToReturnBike(GeoPoint point) {
        //get the list of closest stations
        // goes through the list
        // if a station has more than 40% of ratio recomend it
        List<Station> stations = validStationsToReturnBike(infraestructureManager.consultStations());
        List<StationData> temp, stationdat;
        List<Recommendation> result;
        stationdat = calculateDataReturn(stations, point);

        if (!stationdat.isEmpty()) {
            temp = stationdat.stream().sorted(byUtility()).collect(Collectors.toList());
            result = temp.stream().map(s -> new Recommendation(s.station, 0.0)).collect(Collectors.toList());
        } else {
            throw new RuntimeException("no recomended station");
        }
        return result;
    }

    private List<StationData> calculateDataTake(List<Station> stations, GeoPoint point) {
        List<StationData> aux = new ArrayList<StationData>();
        double closeststationdistance = Double.MAX_VALUE;
        for (Station s : stations) {
            StationData sd = new StationData();
            sd.station = s;
            sd.capacity = s.getCapacity();
            sd.bikes = s.availableBikes();
            sd.distance = s.getPosition().distanceTo(point);
            if (closeststationdistance >= sd.distance) {
                closeststationdistance = sd.distance;
            }
            aux.add(sd);
        }

        //now calculate utility
        double MAXDIST = 500;
        for (StationData sd : aux) {
            //calculate distance utility
            double distanceUtility = calculateDistanceUtility(closeststationdistance, sd.distance);
            //calculate stationutility
            double stationUtility = calculateStationUtilityTake(sd);
            //set utility
            sd.utility = stationUtility * distanceUtility;
        }
        return aux;
    }
    
    private List<StationData> calculateDataReturn(List<Station> stations, GeoPoint point) {
        List<StationData> aux = new ArrayList<StationData>();
        double closeststationdistance = Double.MAX_VALUE;
        for (Station s : stations) {
            StationData sd = new StationData();
            sd.station = s;
            sd.capacity = s.getCapacity();
            sd.bikes = s.availableBikes();
            sd.distance = s.getPosition().distanceTo(point);
            if (closeststationdistance >= sd.distance) {
                closeststationdistance = sd.distance;
            }
            aux.add(sd);
        }

        //now calculate utility
        double MAXDIST = 500;
        for (StationData sd : aux) {
            //calculate distance utility
            double distanceUtility = calculateDistanceUtility(closeststationdistance, sd.distance);
            //calculate stationutility
            double stationUtility = calculateStationUtilityReturn(sd);
            //set utility
            sd.utility = stationUtility * distanceUtility;
        }
        return aux;
    }

    //maximum difference to teh closest station
    private static double MAXDIFF = 500;
    //maximum distance to be recomended (except closest station which would be recomended in any case)
    private static double MAXTOTALDIST = 1000;

    private double calculateDistanceUtility(double closest, double newdist) {
        if (newdist == closest) {
            return 1.0;
        }
        if (newdist > MAXTOTALDIST) {
            return 0.0;
        }
        if (newdist > closest + MAXDIFF) {
            return 0.0;
        }
        return 1 - ((newdist - closest) / MAXDIFF);
    }
    
    private double calculateStationUtilityTake(StationData sd) {
        int halfcap= (int) Math.ceil(((double) sd.capacity) /2.0D);
        if (sd.station.availableBikes()>=halfcap) {
            return 1;
        }
        return ((double)sd.station.availableBikes() / (double) halfcap);
    }

    private double calculateStationUtilityReturn(StationData sd) {
        int halfcap= (int) Math.ceil(((double) sd.capacity) /2.0D);
    
        if (sd.station.availableSlots()>=halfcap) {
            return 1;
        }
        return (sd.station.availableSlots() / halfcap);
    }

}
