package es.urjc.ia.bikesurbanfleets.consultSystems.recommendationSystemTypes;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
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
@RecommendationSystemType("AVAILABLE_RESOURCES_RATIO_PAPER")
public class RecommendationSystemByAvailableResourcesRatioPaper extends RecommendationSystem {

    @RecommendationSystemParameters
    public class RecommendationParameters {

        /**
         * It is the maximum distance in meters between the recommended stations
         * and the indicated geographical point.
         */
        private int maxDistanceRecommendation = 650;

    }

    private RecommendationParameters parameters;

    public RecommendationSystemByAvailableResourcesRatioPaper(JsonObject recomenderdef, InfraestructureManager infraestructureManager) throws Exception {
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
        List<Station> temp;
        List<Recommendation> result = new ArrayList<>();
        List<Station> stations = validStationsToRentBike(infraestructureManager.consultStations()).stream()
                .filter(station -> station.getPosition().distanceTo(point) <= parameters.maxDistanceRecommendation).collect(Collectors.toList());

        if (!stations.isEmpty()) {
            Comparator<Station> byBikesRatio = StationComparator.byBikesCapacityRatio();
            temp = stations.stream().sorted(byBikesRatio).collect(Collectors.toList());
            temp.forEach(s -> System.out.println("Station " + s.getId() + ": " + (double) s.availableBikes() / s.getCapacity()));
            result = temp.stream().map(station -> new Recommendation(station, 0.0)).collect(Collectors.toList());
        }
        return result;
    }

    public List<Recommendation> recommendStationToReturnBike(GeoPoint point) {
        List<Station> temp;
        List<Recommendation> result = new ArrayList<>();
        List<Station> stations = validStationsToReturnBike(infraestructureManager.consultStations()).stream().filter(station -> station.getPosition().distanceTo(point) <= parameters.maxDistanceRecommendation).collect(Collectors.toList());

        if (!stations.isEmpty()) {
            Comparator<Station> bySlotsRatio = StationComparator.bySlotsCapacityRatio();
            temp = stations.stream().sorted(bySlotsRatio).collect(Collectors.toList());
            temp.forEach(s -> System.out.println("Station " + s.getId() + ": " + s.availableBikes() / s.getCapacity()));
            result = temp.stream().map(s -> new Recommendation(s, 0.0)).collect(Collectors.toList());
        }

        return result;
    }

}
