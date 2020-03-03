package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.simple;

import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationUtilityData;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.Recommendation;
import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex.not_used_alternatives_not_better.UtilitiesGlobalLocalUtilityMethods;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.StationManager;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;

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
@RecommendationSystemType("GLOBAL_UTILITY_W_DISTANCE")
public class RecommendationSystemGlobalUtilitiesWithDistance extends RecommendationSystem {

    public class RecommendationParameters {

        /**
         * It is the maximum distance in meters between the recommended stations
         * and the indicated geographical point.
         */
        private int MaxDistanceNormalizer=600;
        private double wheightDistanceStationUtility = 0.35;

        @Override
        public String toString() {
            return " MaxDistanceNormalizer=" + MaxDistanceNormalizer + ", wheightDistanceStationUtility=" + wheightDistanceStationUtility ;
        }

    }
    public String getParameterString(){
        return "RecommendationSystemGlobalUtilitiesWithDistance Parameters{"+ this.parameters.toString() + "}";
    }

    private RecommendationParameters parameters;

    public RecommendationSystemGlobalUtilitiesWithDistance(JsonObject recomenderdef, SimulationServices ss) throws Exception {
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
    }

    @Override
    public List<Recommendation> recommendStationToRentBike(GeoPoint point, double maxdist) {
        List<Recommendation> result ;
        List<Station> candidatestations = stationsWithBikesInWalkingDistance( point,  maxdist);

        if (!candidatestations.isEmpty()) {
            List<StationUtilityData> su = getStationUtility(candidatestations, point, true);
            List<StationUtilityData> temp = su.stream().sorted(DescUtility).collect(Collectors.toList());
            if (printHints) {
                printRecomendations(temp, true);
            }
            result = temp.stream().map(sq -> new Recommendation(sq.getStation(), null)).collect(Collectors.toList());
        } else {
            result = new ArrayList<>(0);
        }
        return result;
    }

    public List<Recommendation> recommendStationToReturnBike(GeoPoint currentposition, GeoPoint destination) {
        List<Recommendation> result ;
        List<Station> candidatestations = stationsWithSlots();

        if (!candidatestations.isEmpty()) {
            List<StationUtilityData> su = getStationUtility(candidatestations, destination, false);
            List<StationUtilityData> temp = su.stream().sorted(DescUtility).collect(Collectors.toList());
            if (printHints) {
                printRecomendations(temp, false);
            }
            result = temp.stream().map(sq -> new Recommendation(sq.getStation(), null)).collect(Collectors.toList());
        } else {
           result = new ArrayList<>(0);
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
                System.out.format("Station %3d %2d %2d %10.2f %9.8f %9.8f %n", +s.getStation().getId(),
                        s.getStation().availableBikes(),
                        s.getStation().getCapacity(),
                        s.getWalkdist(),
                        s.getUtility(),
                        s.getOptimalocupation());
            }
        }
    }

    public List<StationUtilityData> getStationUtility(List<Station> stations, GeoPoint point, boolean rentbike) {
        List<StationUtilityData> temp = new ArrayList<>();
        for (Station s : stations) {
            double idealAvailable = s.getCapacity() / 2D;
            double utildif = UtilitiesGlobalLocalUtilityMethods.calculateClosedSquaredStationUtilityDifferencewithoutDemand(s, rentbike);
            double normedUtilityDiff = utildif * ((double) s.getCapacity() / (double) stationManager.getMaxStationCapacity());
            double maxcap=stationManager.getMaxStationCapacity();
            double utilitymax=(maxcap-1)*4/(maxcap*maxcap);
            double utilitynorm=(normedUtilityDiff+utilitymax)/(2*utilitymax);
            double dist = graphManager.estimateDistance(s.getPosition(),point ,"foot");
            double norm_distance=1-(dist / parameters.MaxDistanceNormalizer);
            double globalutility=parameters.wheightDistanceStationUtility*norm_distance+
                    (1-parameters.wheightDistanceStationUtility)*(utilitynorm);
            StationUtilityData sd = new StationUtilityData(s);
            sd.setUtility(globalutility);
            sd.setOptimalocupation(idealAvailable);
            sd.setWalkdist(dist);
            temp.add(sd);

        }
        return temp;
    }
    Comparator<StationUtilityData> DescUtility1 = (sq1, sq2) -> {
		return  Double.compare(sq1.getWalkdist()/
                                                     sq1.getUtility(),
                                                  sq2.getWalkdist()/sq2.getUtility());
    };
       Comparator<StationUtilityData> DescUtility = (sq1, sq2) -> Double.compare(sq2.getUtility(), sq1.getUtility());

}
