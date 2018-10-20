package es.urjc.ia.bikesurbanfleets.consultSystems.recommendationSystemTypes;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.common.util.SimpleRandom;
import es.urjc.ia.bikesurbanfleets.comparators.StationComparator;
import es.urjc.ia.bikesurbanfleets.consultSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.consultSystems.RecommendationSystemParameters;
import es.urjc.ia.bikesurbanfleets.consultSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.infraestructure.InfraestructureManager;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;

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
@RecommendationSystemType("AVAILABLE_RESOURCES_RATIO")
public class RecommendationSystemByAvailableResourcesRatio extends RecommendationSystem {

    @RecommendationSystemParameters
    public class RecommendationParameters {

        /**
         * It is the maximum distance in meters between the recommended stations
         * and the indicated geographical point.
         */
        private int maxDistanceRecommendation = 800;
        /**
         * It indicates the number of stations to consider when choosing one
         * randomly in recommendation by ratio between available resources and
         * station capacity.
         */
        private int N_STATIONS = 5;

    }

    private RecommendationParameters parameters;

    /**
     * It contains several comparators to sort stations.
     */
    private SimpleRandom rand;

    public RecommendationSystemByAvailableResourcesRatio(JsonObject recomenderdef, InfraestructureManager infraestructureManager) throws Exception {
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
       this.rand = new SimpleRandom(1);
    }

    private List<Station> fartherStations(GeoPoint point, List<Station> stations) {
        return stations.stream().filter(station -> station.getPosition().distanceTo(point) > parameters.maxDistanceRecommendation)
                .collect(Collectors.toList());
    }

    private List<Station> nearerStations(GeoPoint point, List<Station> stations) {
        return stations.stream().filter(station -> station.getPosition().distanceTo(point) <= parameters.maxDistanceRecommendation)
                .collect(Collectors.toList());
    }

    private List<Station> rebalanceWhenRenting(List<Station> stations) {
        double ratioSum = 0.0;
        int i;
        int n_stations = stations.size() > parameters.N_STATIONS ? parameters.N_STATIONS : stations.size();
        for (i = 0; i < n_stations; i++) {
            ratioSum += stations.get(i).availableBikes() / stations.get(i).getCapacity();
        }

        double random = rand.nextDouble(0, ratioSum);
        double ratio;
        for (i = 0; i < n_stations; i++) {
            ratio = stations.get(i).availableBikes() / stations.get(i).getCapacity();
            if (random <= ratio) {
                break;
            }
            random -= ratio;
        }
        Station selected = stations.remove(i);
        stations.add(0, selected);
        return stations;
    }

    private List<Station> rebalanceWhenReturning(List<Station> stations) {
        double ratioSum = 0.0;
        int i;
        int n_stations = stations.size() > parameters.N_STATIONS ? parameters.N_STATIONS : stations.size();
        for (i = 0; i < n_stations; i++) {
            ratioSum += stations.get(i).availableSlots() / stations.get(i).getCapacity();
        }

        double random = rand.nextDouble(0, ratioSum);
        double ratio;
        for (i = 0; i < n_stations; i++) {
            ratio = stations.get(i).availableSlots() / stations.get(i).getCapacity();
            if (random <= ratio) {
                break;
            }
            random -= ratio;
        }
        Station selected = stations.remove(i);
        stations.add(0, selected);
        return stations;
    }

    @Override
    public List<Recommendation> recommendStationToRentBike(GeoPoint point) {
        List<Station> stations = validStationsToRentBike(infraestructureManager.consultStations());
        List<Station> temp;
        List<Recommendation> result = new ArrayList<>();
        if (!stations.isEmpty()) {
            List<Station> nearer = nearerStations(point, stations);
            List<Station> farther = fartherStations(point, stations);
            Comparator<Station> byBikesRatio = StationComparator.byBikesCapacityRatio();
            nearer = nearer.stream().sorted(byBikesRatio).collect(Collectors.toList());
            farther = farther.stream().sorted(byBikesRatio).collect(Collectors.toList());

            nearer.addAll(farther);
            temp = rebalanceWhenRenting(nearer);
            result = temp.stream().map(station -> new Recommendation(station, 0.0)).collect(Collectors.toList());
        }
        return result;
    }

    public List<Recommendation> recommendStationToReturnBike(GeoPoint point) {
        List<Station> stations = validStationsToReturnBike(infraestructureManager.consultStations());
        List<Station> temp;
        List<Recommendation> result = new ArrayList<>();
        if (!stations.isEmpty()) {
            List<Station> nearer = nearerStations(point, stations);
            List<Station> farther = fartherStations(point, stations);
            Comparator<Station> bySlotsRatio = StationComparator.bySlotsCapacityRatio();
            nearer = nearer.stream().sorted(bySlotsRatio).collect(Collectors.toList());
            farther = farther.stream().sorted(bySlotsRatio).collect(Collectors.toList());
            nearer.addAll(farther);
            temp = rebalanceWhenReturning(nearer);
            result = temp.stream().map(station -> new Recommendation(station, 0.0)).collect(Collectors.toList());
        }

        return result;
    }

}
