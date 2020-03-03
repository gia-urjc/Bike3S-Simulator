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

    public static class RecommendationParameters extends RecommendationSystem.RecommendationParameters{
        /**
         * It is the maximum distance in meters between the recommended stations
         * and the indicated geographical point.
         */
        private int MaxDistanceNormalizer=600;
        private double wheightDistanceStationUtility = 0.35;
    }

    private RecommendationParameters parameters;

    public RecommendationSystemGlobalUtilitiesWithDistance(JsonObject recomenderdef, SimulationServices ss) throws Exception {
        //***********Parameter treatment*****************************
        //parameters are read in the superclass
        //afterwards, they have to be cast to this parameters class
        super(recomenderdef, ss, new RecommendationParameters());
        this.parameters= (RecommendationParameters)(super.parameters);
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
