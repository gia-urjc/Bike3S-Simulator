package es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.simpleRecommendationSystemTypes;

import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.StationUtilityData;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.Recommendation;
import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.core.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.ComplexRecommendationSystemTypes.UtilitiesForRecommendationSystems;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystemParameters;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.InfrastructureManager;
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
@RecommendationSystemType("LOCAL_UTILITY_W_DISTANCE")
public class RecommendationSystemLocalUtilitiesWithDistance extends RecommendationSystem {

    @RecommendationSystemParameters
    public class RecommendationParameters {

        /**
         * It is the maximum distance in meters between the recommended stations
         * and the indicated geographical point.
         */
        private int maxDistanceRecommendation = 600;
        private int MaxDistanceNormalizer=600;
        private double wheightDistanceStationUtility = 0.35;

        @Override
        public String toString() {
            return  "maxDistanceRecommendation=" + maxDistanceRecommendation + ", MaxDistanceNormalizer=" + MaxDistanceNormalizer + ", wheightDistanceStationUtility=" + wheightDistanceStationUtility ;
        }
 
    }
    public String getParameterString(){
        return "RecommendationSystemLocalUtilitiesWithDistance Parameters{"+ this.parameters.toString() + "}";
    }

    private RecommendationParameters parameters;
    boolean printHints = true;

    public RecommendationSystemLocalUtilitiesWithDistance(JsonObject recomenderdef, SimulationServices ss) throws Exception {
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
    public List<Recommendation> recommendStationToRentBike(GeoPoint point) {
        List<Recommendation> result;
        List<Station> stations = validStationsToRentBike(infrastructureManager.consultStations()).stream()
                .filter(station -> station.getPosition().distanceTo(point) <= parameters.maxDistanceRecommendation).collect(Collectors.toList());

        if (!stations.isEmpty()) {
            List<StationUtilityData> su = getStationUtility(stations, point, true);
            List<StationUtilityData> temp = su.stream().sorted(DescUtility).collect(Collectors.toList());
            if (printHints) {
                printRecomendations(temp, true);
            }
            result = temp.stream().map(sq -> new Recommendation(sq.getStation(), null)).collect(Collectors.toList());
        } else {
            result = new ArrayList<>();
            System.out.println("no recommendation for take at Time:" + SimulationDateTime.getCurrentSimulationDateTime());
        }
        return result;
    }

    public List<Recommendation> recommendStationToReturnBike(GeoPoint currentposition, GeoPoint destination) {
        List<Recommendation> result = new ArrayList<>();;
        List<Station> stations = validStationsToReturnBike(infrastructureManager.consultStations()).stream().collect(Collectors.toList());

        if (!stations.isEmpty()) {
            List<StationUtilityData> su = getStationUtility(stations, destination, false);
            List<StationUtilityData> temp = su.stream().sorted(DescUtility).collect(Collectors.toList());
            if (printHints) {
                printRecomendations(temp, false);
            }
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
            System.out.format("Station %3d %2d %2d %10.2f %9.8f %9.8f %n", +s.getStation().getId(),
                    s.getStation().availableBikes(),
                    s.getStation().getCapacity(),
                    s.getWalkdist(),
                    s.getUtility(),
                    s.getOptimalocupation());
            }
        }
    }

    public List<StationUtilityData> getStationUtility(List<Station> stations,GeoPoint point, boolean rentbike) {
        List<StationUtilityData> temp=new ArrayList<>();
        for (Station s:stations){
            double utildif = UtilitiesForRecommendationSystems.calculateClosedSquaredStationUtilityDifferencewithoutDemand(s, rentbike);
            double idealAvailable=s.getCapacity()/2D;
            double utilitymax=(2*idealAvailable-1)/(idealAvailable*idealAvailable);
            double utilitynorm=(utildif+utilitymax)/(2*utilitymax);
            double dist = point.distanceTo(s.getPosition());
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
