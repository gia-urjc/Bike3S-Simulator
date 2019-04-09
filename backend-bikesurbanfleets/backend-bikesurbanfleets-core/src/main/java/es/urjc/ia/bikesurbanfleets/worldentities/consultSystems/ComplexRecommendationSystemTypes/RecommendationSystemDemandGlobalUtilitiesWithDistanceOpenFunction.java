package es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.ComplexRecommendationSystemTypes;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.core.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystemParameters;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.Recommendation;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.StationUtilityData;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Station;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is a system which recommends the user the stations to which he
 * should go to contribute with system rebalancing. Then, this recommendation
 * system gives the user a list of stations ordered descending by the
 * "resources/capacityº" ratio.
 *
 * @author IAgroup
 *
 */
@RecommendationSystemType("GLOBAL_UTILITY_W_DISTANCE_DEMAND_OPENFUNCTION")
public class RecommendationSystemDemandGlobalUtilitiesWithDistanceOpenFunction extends RecommendationSystem {

    @RecommendationSystemParameters
    public class RecommendationParameters {

        /**
         * It is the maximum distance in meters between the recommended stations
         * and the indicated geographical point.
         */
        private int maxDistanceRecommendation = 600;
        private double wheightDistanceStationUtility = 0.3;

    }

    private RecommendationParameters parameters;
    private UtilitiesForRecommendationSystems recutils;
    boolean printHints=false;

    public RecommendationSystemDemandGlobalUtilitiesWithDistanceOpenFunction(JsonObject recomenderdef, SimulationServices ss) throws Exception {
        super(ss);
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
        recutils=new UtilitiesForRecommendationSystems(this);
    }

    @Override
    public List<Recommendation> recommendStationToRentBike(GeoPoint point) {
        List<Recommendation> result;
        List<Station> stations = validStationsToRentBike(infrastructureManager.consultStations()).stream()
                .filter(station -> station.getPosition().distanceTo(point) <= parameters.maxDistanceRecommendation).collect(Collectors.toList());

        if (!stations.isEmpty()) {
            List<StationUtilityData> su = getStationUtility(stations, point, true);
            Comparator<StationUtilityData> DescUtility = (sq1, sq2) -> Double.compare(sq2.getUtility(), sq1.getUtility());
            List<StationUtilityData> temp = su.stream().sorted(DescUtility).collect(Collectors.toList());
            if (printHints) printRecomendations(temp, true);
            result = temp.stream().map(sq -> new Recommendation(sq.getStation(), null)).collect(Collectors.toList());
        } else {
            result = new ArrayList<>();
            System.out.println("no recommendation for take at Time:" + SimulationDateTime.getCurrentSimulationDateTime());
        }
        return result;
    }

    public List<Recommendation> recommendStationToReturnBike(GeoPoint currentposition, GeoPoint destination) {
        List<Recommendation> result = new ArrayList<>();
        List<Station> stations = validStationsToReturnBike(infrastructureManager.consultStations()).stream().
                filter(station -> station.getPosition().distanceTo(destination) <= parameters.maxDistanceRecommendation).collect(Collectors.toList());

        if (!stations.isEmpty()) {
            List<StationUtilityData> su = getStationUtility(stations, destination, false);
            Comparator<StationUtilityData> byDescUtilityIncrement = (sq1, sq2) -> Double.compare(sq2.getUtility(), sq1.getUtility());
            List<StationUtilityData> temp = su.stream().sorted(byDescUtilityIncrement).collect(Collectors.toList());
            if (printHints) printRecomendations(temp, false);
            result = temp.stream().map(sq -> new Recommendation(sq.getStation(), null)).collect(Collectors.toList());
        } else {
            System.out.println("no recommendation for return at Time:" + SimulationDateTime.getCurrentSimulationDateTime());
        }
        return result;
    }
    
    private void printRecomendations(List<StationUtilityData> su, boolean take) {
        if (printHints) {
        int max = su.size();//Math.min(5, su.size());
        System.out.println();
        if (take) {
            System.out.println("Time (take):" + SimulationDateTime.getCurrentSimulationDateTime());
        } else {
            System.out.println("Time (return):" + SimulationDateTime.getCurrentSimulationDateTime());
        }
        for (int i = 0; i < max; i++) {
            StationUtilityData s = su.get(i);
            System.out.format("Station %3d %2d %2d %10.2f %9.8f %6f %6f %6f %n", +s.getStation().getId(),
                    s.getStation().availableBikes(),
                    s.getStation().getCapacity(),
                    s.getWalkdist(),
                    s.getUtility(),
                    s.getMinoptimalocupation() ,
                    s.getOptimalocupation() ,
                    s.getMaxopimalocupation());
            }
        }
    }
    public List<StationUtilityData> getStationUtility(List<Station> stations, GeoPoint point, boolean rentbike) {
        double currentglobalbikedemand=recutils.getCurrentGlobalBikeDemand();
        List<StationUtilityData> temp = new ArrayList<>();
        for (Station s : stations) {

            StationUtilityData sd = new StationUtilityData(s);
            double idealbikes = recutils.getCurrentBikeDemand(s);
            double maxidealbikes = s.getCapacity() -recutils.getCurrentSlotDemand(s);

            double utility = getUtility(s, 0, idealbikes, maxidealbikes);
            double newutility;
            if (rentbike) {
                newutility = getUtility(s, -1, idealbikes, maxidealbikes);
            } else {//return bike 
                newutility = getUtility(s, +1, idealbikes, maxidealbikes);
            }
            double normedUtilityDiff = (newutility - utility)
                    * (idealbikes/ currentglobalbikedemand) * infrastructureManager.getNumberStations();
//                    * (idealbikes/ ud.maxdemand) ;

            double dist = point.distanceTo(s.getPosition());
            double norm_distance = 1 - normatizeTo01(dist, 0, parameters.maxDistanceRecommendation);
            double globalutility = parameters.wheightDistanceStationUtility * norm_distance
                    + (1 - parameters.wheightDistanceStationUtility) * normedUtilityDiff;

            /*       double mincap=(double)infraestructureManager.getMinStationCapacity();
            double maxinc=(4D*(mincap-1))/Math.pow(mincap,2);
            double auxnormutil=((newutility-utility+maxinc)/(2*maxinc));
            double globalutility= dist/auxnormutil; 
             */
            sd.setUtility(globalutility);
            sd.setMaxopimalocupation(maxidealbikes);
            sd.setMinoptimalocupation(idealbikes);
            if (idealbikes > maxidealbikes) {
                sd.setOptimalocupation((idealbikes + maxidealbikes) / 2D);
            } else {
                sd.setOptimalocupation(Double.NaN);
            }
            sd.setWalkdist(dist);
            temp.add(sd);
        }
        return temp;
    }

    private double getUtility(Station s, int bikeincrement, double idealbikes, double maxidealbikes) {
        double cap = s.getCapacity();
        double ocupation = s.availableBikes() + bikeincrement;
        if (idealbikes <= maxidealbikes) {
            if (ocupation <= idealbikes) {
                return 1 - Math.pow(((ocupation - idealbikes) / idealbikes), 2);
            } else if (ocupation >= maxidealbikes) {
                return 1 - Math.pow(((ocupation - maxidealbikes) / (cap - maxidealbikes)), 2);
            } else {//if ocupation is just between max and min
                return 1;
            }
        } else { //idealbikes > max idealbikes
            double bestocupation = (idealbikes  + maxidealbikes) / 2D;
  //          double bestocupation = (idealbikes * cap)/(cap - maxidealbikes  ) ;
            if (ocupation <= bestocupation) {
                return 1 - Math.pow(((ocupation - bestocupation) / bestocupation), 2);
            } else {
                double aux = cap - bestocupation;
                return 1 - Math.pow(((ocupation - bestocupation) / aux), 2);
            }
        }
    }
}
