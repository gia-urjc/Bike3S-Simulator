package es.urjc.ia.bikesurbanfleets.consultSystems.recommendationSystemTypes;

import com.google.gson.JsonObject;
import java.util.ArrayList;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.consultSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.consultSystems.RecommendationSystemParameters;
import es.urjc.ia.bikesurbanfleets.consultSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.infraestructure.InfraestructureManager;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.log.Debug;

@RecommendationSystemType("HOLGERRECOMENDER")
public class HolgerRecomender extends RecommendationSystem {

    class StationData {

        public Station station = null;
        double distance = 0.0;
        double stationUtility = 0.0;
        double distanceutility = 0.0;
        double utility = 0.0;
    }

    static public Comparator<StationData> byUtility() {
        return (s1, s2) -> Double.compare(s2.utility, s1.utility);
    }

    @RecommendationSystemParameters
    class RecommendationParameters {

        //maximum difference to teh closest station
        private double MAXDIFF = 500;
        //maximum distance to be recomended (except closest station which would be recomended in any case)
        private double MAXTOTALDIST = 1000;

    }

    RecommendationParameters parameters;

    public HolgerRecomender(JsonObject recomenderdef, InfraestructureManager infraestructureManager) throws Exception {
        super(infraestructureManager);
        //***********Parameter treatment*****************************
        //if this recomender has parameters this is the right declaration
        //if no parameters are used this code just has to be commented
        //"getparameters" is defined in USER such that a value of Parameters 
        // is overwritten if there is a values specified in the jason description of the recomender
        // if no value is specified in jason, then the orriginal value of that field is mantained
        // that means that teh paramerts are all optional
        // if you want another behaviour, then you should overwrite getParameters in this calss
        this.parameters = new RecommendationParameters();
        getParameters(recomenderdef, this.parameters);
    }

    @Override
    public List<Recommendation> recommendStationToRentBike(GeoPoint point) {
        //get the list of closest stations
        // goes through the list
        List<StationData> stationdat = new ArrayList<StationData>();
        double closesStationDistance = getBasicStationData(point, true, stationdat);
        double equlibrium = calculateStationBasicUtilities(stationdat, true);
        calculateFinalUtilities(stationdat, equlibrium, closesStationDistance);

        if (!stationdat.isEmpty()) {
            List<StationData> temp = stationdat.stream().sorted(byUtility()).collect(Collectors.toList());
            if (Debug.isDebugmode()) {
                for (int i = 0; i < 10; i++) {
                    System.out.println("stat " + i + " utility: " + temp.get(i).utility);
                    System.out.println("      dist: " + temp.get(i).distance);
                    System.out.println("      bikes: " + temp.get(i).station.availableBikes());
                    System.out.println("      dist_utility: " + temp.get(i).distanceutility);
                    System.out.println("      station_utility: " + temp.get(i).stationUtility);
                }
            }
            return temp.stream().map(s -> new Recommendation(s.station,null)).collect(Collectors.toList());
        } else {
            throw new RuntimeException("no recomended station");
        }
    }

    @Override
    public List<Recommendation> recommendStationToReturnBike(GeoPoint point) {
        //get the list of closest stations
        // goes through the list
        // if a station has more than 40% of ratio recomend it
        List<StationData> stationdat = new ArrayList<StationData>();
        double closesStationDistance = getBasicStationData(point, false, stationdat);
        double equlibrium = calculateStationBasicUtilities(stationdat, false);
        calculateFinalUtilities(stationdat, equlibrium, closesStationDistance);

        if (!stationdat.isEmpty()) {
            List<StationData> temp = stationdat.stream().sorted(byUtility()).collect(Collectors.toList());
            return temp.stream().map(s -> new Recommendation(s.station, null)).collect(Collectors.toList());
        } else {
            throw new RuntimeException("no recomended station");
        }
    }

    //puts the basic distance data and calculates the closest station distance
    //if retnt=true is for taking, else for leaving bike
    protected double getBasicStationData(GeoPoint point, boolean rent, List<StationData> stationdat) {
        double closesStationDistance = 0.0D;
        stationdat.clear();
        //get stations that have bikes/slots
        List<Station> stations;
        if (rent) {
            stations = validStationsToRentBike(infraestructureManager.consultStations());
        } else {
            stations = validStationsToReturnBike(infraestructureManager.consultStations());
        }

        //setup basic station data (distance and get closest stationdistance)
        closesStationDistance = Double.MAX_VALUE;
        for (Station s : stations) {
            StationData sd = new StationData();
            sd.station = s;
            sd.distance = s.getPosition().distanceTo(point);
            if (closesStationDistance >= sd.distance) {
                closesStationDistance = sd.distance;
            }
            stationdat.add(sd);
        }
        return closesStationDistance;
    }

    protected void calculateFinalUtilities(List<StationData> stations, double stationutilityequilibrium, double closestsdistance) {

        for (StationData sd : stations) {
            //calculate distance utility
            sd.distanceutility = calculateDistanceUtility(closestsdistance, sd.distance);
            //calculate stationutility
            sd.stationUtility = normatizeToUtility(sd.stationUtility, 0, stationutilityequilibrium);
            //set utility
            sd.utility = sd.stationUtility * sd.distanceutility;
        }
    }

    //calculates the unnormalized station utilities of all stations
    //returns the average stationutility of all stations
    protected double calculateStationBasicUtilities(List<StationData> stations, boolean rent) {

        //now calculate utility
        double stationutilsum = 0.0D;
        double numberstations = 0.0D;
        if (rent) {
            for (StationData sd : stations) {
                //calculate stationutility
                sd.stationUtility = sd.station.availableBikes();
                stationutilsum += sd.stationUtility;
                numberstations++;
            }
        } else {
            for (StationData sd : stations) {
                //calculate stationutility
                sd.stationUtility = sd.station.availableSlots();
                stationutilsum += sd.stationUtility;
                numberstations++;
            }
        }
        return stationutilsum / numberstations;
    }

    protected double calculateDistanceUtility(double closest, double newdist) {
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
        //  return   50D/(Math.pow(newdist - closest,1.1D)+51D);
    }

}
