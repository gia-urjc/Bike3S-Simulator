package es.urjc.ia.bikesurbanfleets.consultSystems.recommendationSystemTypes;

import com.google.gson.JsonObject;
import java.util.ArrayList;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.common.util.SimpleRandom;
import es.urjc.ia.bikesurbanfleets.consultSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.consultSystems.RecommendationSystemParameters;
import es.urjc.ia.bikesurbanfleets.consultSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.infraestructure.InfraestructureManager;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;

@RecommendationSystemType("HOLGERRECOMENDER_RANDOM")
public class HolgerRecomenderRandom extends RecommendationSystem {

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
        private int MINCAP_TO_RECOMEND=5;
        //maximum difference to teh closest station
        private  double MAXDIFF = 500;
        //maximum distance to be recomended (except closest station which would be recomended in any case)
        private  double MAXTOTALDIST = 1000;

    }

    private RecommendationParameters parameters;
    private SimpleRandom rand;

    public HolgerRecomenderRandom (JsonObject recomenderdef, InfraestructureManager infraestructureManager) throws Exception{
        super(infraestructureManager);
       //***********Parameter treatment*****************************
        //if this recomender has parameters this is the right declaration
        //if no parameters are used this code just has to be commented
        //"getparameters" is defined in USER such that a value of Parameters 
        // is overwritten if there is a values specified in the jason description of the recomender
        // if no value is specified in jason, then the orriginal value of that field is mantained
        // that means that teh paramerts are all optional
        // if you want another behaviour, then you should overwrite getParameters in this calss
      this.rand = new SimpleRandom(1);
         this.parameters = new RecommendationParameters();
        getParameters(recomenderdef, this.parameters);
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
            
            for (int i=0; i<10; i++){
                System.out.println("stat "+ i + " utility: " + temp.get(i).utility);  
                System.out.println("      dist: " + temp.get(i).distance);  
                System.out.println("      bikes: " + temp.get(i).station.availableBikes());  
            }
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
        double utilitysum=0.0D;
        for (StationData sd : aux) {
            //calculate distance utility
            double distanceUtility = calculateDistanceUtility(closeststationdistance, sd.distance);
            //calculate stationutility
            double stationUtility = calculateStationUtilityTake(sd);
            //set utility
            sd.utility = stationUtility * distanceUtility;
            utilitysum+=sd.utility;
        }
        selectRandom(aux,utilitysum);

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
        double utilitysum=0.0D;
       for (StationData sd : aux) {
            //calculate distance utility
            double distanceUtility = calculateDistanceUtility(closeststationdistance, sd.distance);
            //calculate stationutility
            double stationUtility = calculateStationUtilityReturn(sd);
            //set utility
            sd.utility = stationUtility * distanceUtility;
            utilitysum+=sd.utility;
        }
       
       selectRandom(aux,utilitysum);
       return aux;
    }

    private void selectRandom(List<StationData> aux, double utilitysum){
        //set the utility of one station randomly to 1.1 (highest)
        double aux2=rand.nextDouble(0.0, utilitysum);
        double aux3=0.0D;
        for (StationData sd : aux) {
            aux3+=sd.utility;
            if (aux3>=aux2){
                sd.utility=1.1D;
                break;
            }
        }
    } 

    private double calculateDistanceUtility(double closest, double newdist) {
        if (newdist == closest) {
            return 1.0;
        }
        if (newdist > parameters.MAXTOTALDIST) {
            return 0.0;
        }
        if (newdist > closest + parameters.MAXDIFF) {
            return 0.0;
        }
        return 1 - ((newdist - closest) / parameters.MAXDIFF);
    }
    
    private double calculateStationUtilityTake(StationData sd) {
    //    int halfcap= (int) Math.ceil(((double) sd.capacity) /3.5D);
        if (sd.station.availableBikes()>=parameters.MINCAP_TO_RECOMEND) {
            return 1;
        }
        return ((double)sd.station.availableBikes() / (double) parameters.MINCAP_TO_RECOMEND);
    }

    private double calculateStationUtilityReturn(StationData sd) {
    //    int halfcap= (int) Math.ceil(((double) sd.capacity) /8D);
    
        if (sd.station.availableSlots()>=parameters.MINCAP_TO_RECOMEND) {
            return 1;
        }
        return ((double)sd.station.availableSlots() / (double)parameters.MINCAP_TO_RECOMEND);
    }

}
