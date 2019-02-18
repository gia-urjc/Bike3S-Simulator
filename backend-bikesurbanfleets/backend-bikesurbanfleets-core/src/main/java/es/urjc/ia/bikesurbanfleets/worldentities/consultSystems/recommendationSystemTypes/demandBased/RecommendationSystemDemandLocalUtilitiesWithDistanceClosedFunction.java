package es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.recommendationSystemTypes.demandBased;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.demand.DemandManager;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.core.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.worldentities.comparators.StationComparator;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystemParameters;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.recommendationSystemTypes.Recommendation;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.recommendationSystemTypes.StationUtilityData;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.InfrastructureManager;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Station;
import java.time.LocalDateTime;

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
@RecommendationSystemType("LOCAL_UTILITY_W_DISTANCE_DEMAND_CLOSEDFUNCTION")
public class RecommendationSystemDemandLocalUtilitiesWithDistanceClosedFunction extends RecommendationSystem {

    
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

    public RecommendationSystemDemandLocalUtilitiesWithDistanceClosedFunction(JsonObject recomenderdef, SimulationServices ss) throws Exception {
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
        //       demandManager=infraestructureManager.getDemandManager();
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
            System.out.println();
            temp.forEach(s -> System.out.println("Station (take)" + s.getStation().getId() + ": "
                    + s.getStation().availableBikes() + " "
                    + s.getStation().getCapacity() + " " 
                    + s.getOptimalocupation() + " "
                    + s.getDistance() + " "
                    + s.getUtility() ));
            result = temp.stream().map(sq -> new Recommendation(sq.getStation(), null)).collect(Collectors.toList());
        } else {
            result = new ArrayList<>();
        }
        return result;
    }

    public List<Recommendation> recommendStationToReturnBike(GeoPoint point) {
        List<Recommendation> result = new ArrayList<>();
        List<Station> stations = validStationsToReturnBike(infrastructureManager.consultStations()).stream().
                filter(station -> station.getPosition().distanceTo(point) <= parameters.maxDistanceRecommendation).collect(Collectors.toList());

        if (!stations.isEmpty()) {
            List<StationUtilityData> su = getStationUtility(stations, point, false);
            Comparator<StationUtilityData> byDescUtilityIncrement = (sq1, sq2) -> Double.compare(sq2.getUtility(), sq1.getUtility());
            List<StationUtilityData> temp = su.stream().sorted(byDescUtilityIncrement).collect(Collectors.toList());
            System.out.println();
            temp.forEach(s -> System.out.println("Station (return)" + s.getStation().getId() + ": "
                    + s.getStation().availableBikes() + " "
                    + s.getStation().getCapacity() + " " 
                    + s.getOptimalocupation() + " "
                    + s.getDistance() + " "
                    + s.getUtility() ));
            result = temp.stream().map(sq -> new Recommendation(sq.getStation(), null)).collect(Collectors.toList());
        } 
        return result;
    }

    public List<StationUtilityData> getStationUtility(List<Station> stations, GeoPoint point, boolean rentbike) {
        List<StationUtilityData> temp = new ArrayList<>();
        for (Station s : stations) {
            
            StationUtilityData sd = new StationUtilityData(s);

            double idealAvailable = getIdealocupation(s.getId(), s.getCapacity(), SimulationDateTime.getCurrentSimulationDateTime()); 
            double utility = getUtility(s.availableBikes(), s.getCapacity(), idealAvailable);
            double newutility;
            if (rentbike) {
                newutility = getUtility(s.availableBikes() - 1, s.getCapacity(), idealAvailable);
            } else {//return bike 
                newutility = getUtility(s.availableBikes() + 1, s.getCapacity(), idealAvailable);
            }
            double dist = point.distanceTo(s.getPosition());
            double norm_distance = 1 - normatizeTo01(dist, 0, parameters.maxDistanceRecommendation);
            double globalutility = parameters.wheightDistanceStationUtility * norm_distance
                    + (1 - parameters.wheightDistanceStationUtility) * (newutility - utility);

            /*       double mincap=(double)infraestructureManager.getMinStationCapacity();
            double maxinc=(4D*(mincap-1))/Math.pow(mincap,2);
            double auxnormutil=((newutility-utility+maxinc)/(2*maxinc));
            double globalutility= dist/auxnormutil; 
             */ sd.setUtility(globalutility); 
             sd.setOptimalocupation(idealAvailable);
            sd.setDistance(dist);
            temp.add(sd);
        }
        return temp;
    }

    private double getIdealocupation(int stationid, int capacity, LocalDateTime current) {
        DemandManager dm=infrastructureManager.getDemandManager();
        DemandManager.DemandResult takedem = dm.getTakeDemandStation(stationid, DemandManager.Month.toDemandMangerMonth(current.getMonth()),
                DemandManager.Day.toDemandMangerDay(current.getDayOfWeek()), current.getHour());
        DemandManager.DemandResult retdem = dm.getReturnDemandStation(stationid, DemandManager.Month.toDemandMangerMonth(current.getMonth()),
                DemandManager.Day.toDemandMangerDay(current.getDayOfWeek()), current.getHour());
        if (takedem.hasDemand() && retdem.hasDemand()) {
            return (takedem.demand() + capacity - retdem.demand())/2D;
        } else {
            System.out.println("[WARNING:] no bike or slot demand data available for station: " + stationid + " at date " + current + ": we assume an optimal ocupation of half the capacity");
            return capacity/2D;
        }
     }
    
    private double getUtility(int ocupation, int capacity, double idealocupation) {
        if (ocupation <= idealocupation) {
            return 1 - Math.pow(((ocupation - idealocupation) / idealocupation), 2);
        } else {
            double aux = capacity - idealocupation;
            return 1 - Math.pow(((ocupation - idealocupation) / aux), 2);
        }
    }
}
