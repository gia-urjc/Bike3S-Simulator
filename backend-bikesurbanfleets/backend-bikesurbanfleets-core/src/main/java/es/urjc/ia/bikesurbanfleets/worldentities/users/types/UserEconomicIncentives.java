package es.urjc.ia.bikesurbanfleets.worldentities.users.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.simple.StationComparator;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.Recommendation;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.Incentives.Incentive;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.Incentives.Money;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserParameters;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserType;
import es.urjc.ia.bikesurbanfleets.worldentities.users.User;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class represents a user who always follows the first system
 * recommendations i. e., that which consist of renting a bike at the station
 * which has more available bikes and returning the bike at the station which
 * has more available slots. This user never reserves neither bikes nor slots at
 * destination stations as he knows that the system is recommending him that
 * station because it is the station which has more available bikes or slots, so
 * he knows that, almost certainly, he'll be able to rent or to return a bike.
 * Moreover, he always chooses the shortest routes to get his destination.
 *
 * @author IAgroup
 */
@UserType("USER_ECONOMIC_INCENTIVES")
public class UserEconomicIncentives extends UserObedient {

    @UserParameters
    public class Parameters {

        //default constructor used if no parameters are specified
        private Parameters() {
        }
        /**
         * It is the number of times that the user will try to rent a bike
         * (without a bike reservation) before deciding to leave the system.
         */
        private int minRentalAttempts = 3;
        private int COMPENSATION = 10;   // 1 incentive unit  per 10 meters
        private int EXTRA = 30;   // 30% of quality
        private int maxdistance = 800; //800 meters
 
        int maxDistanceToRentBike = 600;

        GeoPoint intermediatePosition = null;

        @Override
        public String toString() {
            return "Parameters{"
                    + "minRentalAttempts=" + minRentalAttempts
                    + '}';
        }

    }

    Parameters parameters;

    public UserEconomicIncentives(JsonObject userdef, SimulationServices services, long seed) throws Exception {
        super( userdef, services, seed);
        //***********Parameter treatment*****************************
        //if this user has parameters this is the right declaration
        //if no parameters are used this code just has to be commented
        //"getparameters" is defined in USER such that a value of Parameters 
        // is overwritten if there is a values specified in the jason description of the user
        // if no value is specified in jason, then the orriginal value of that field is mantained
        // that means that teh paramerts are all optional
        // if you want another behaviour, then you should overwrite getParameters in this calss
        this.parameters = new Parameters();
        getParameters(userdef.getAsJsonObject("userType"), this.parameters);
    }

    @Override
    public Station determineStationToRentBike() {
        Station destination = null;
        List<Recommendation> recommendedStations = recommendationSystem.getRecomendedStationsToRentBike(this.getPosition());
        List<Station> stations = informationSystem.getAllStations();
        Station nearestStation = nearestStationToRent(stations, this.getPosition());

        if (!recommendedStations.isEmpty()) {
            int i = 0;
            while (destination == null && i < recommendedStations.size()) {
                Station station = recommendedStations.get(i).getStation();

                Incentive incentive = recommendedStations.get(i).getIncentive();
                double quality = qualityToRent(stations, station);
                double compensation = compensation(this.getPosition(), nearestStation, station);
                double extra = quality * parameters.EXTRA / 100;
                if (incentive instanceof Money) {
                    Money money = (Money) incentive;
                    if (money.getValue() >= (int) (compensation + extra)) {
                        destination = recommendedStations.get(i).getStation();
                    }
                    System.out.println("station " + station.getId());
                    System.out.println("incentive: " + money.getValue());
                    System.out.println("min expected incentive: " + (compensation + extra));
                }
                i++;
            }
        }
        if (destination == null) {
            destination = nearestStation;
        }
        return destination;
    }

    @Override
    public Station determineStationToReturnBike() {
        Station destination = null;
        List<Recommendation> recommendedStations = recommendationSystem.getRecomendedStationsToReturnBike(this.getPosition(), this.getDestinationPlace());
        //Remove station if the user is in this station
        recommendedStations.removeIf(recommendation -> recommendation.getStation().getPosition().equals(this.getPosition()));
        List<Station> stations = informationSystem.getAllStations();
        Station nearestStation = nearestStationToReturn(stations, this.getDestinationPlace());
        if (!recommendedStations.isEmpty()) {
            int i = 0;
            while (destination == null && i < recommendedStations.size()) {
                Station station = recommendedStations.get(i).getStation();
                Incentive incentive = recommendedStations.get(i).getIncentive();
                double quality = qualityToReturn(stations, station);
                double compensation = compensation(this.getDestinationPlace(), nearestStation, station);
                double extra = quality * parameters.EXTRA / 100;
                if (incentive instanceof Money) {
                    Money money = (Money) incentive;
                    if (money.getValue() >= (int) (compensation + extra)) {
                        destination = recommendedStations.get(i).getStation();
                    }
                    System.out.println("station " + station.getId());
                    System.out.println("incentive: " + money.getValue());
                    System.out.println("min expected incentive: " + (compensation + extra));
                }
                i++;
            }
        }
        if (destination == null) {
            destination = nearestStation;
        }
        return destination;
    }


    @Override
    public String toString() {
        return super.toString() + "UserDistanceRestriction{"
                + "parameters=" + parameters
                + '}';
    }

    private double compensation(GeoPoint point, Station nearestStation, Station recommendedStation) {
        double distanceToNearestStation = nearestStation.getPosition().distanceTo(point);
        double distanceToRecommendedStation = recommendedStation.getPosition().distanceTo(point);
        return (distanceToRecommendedStation - distanceToNearestStation) / parameters.COMPENSATION;
    }

    private double qualityToRent(List<Station> stations, Station station) {
        double summation = 0;
        if (!stations.isEmpty()) {
            double factor = 0.0;
            double multiplication = 1.0;
            double maxDistance = parameters.maxdistance;
            double distance = 0.0;
            for (Station s : stations) {
                distance = station.getPosition().distanceTo(s.getPosition());
                if (maxDistance > distance) {
                    factor = (maxDistance - distance) / maxDistance;
                }
                multiplication = s.availableBikes() * factor;
                summation += multiplication;
            }
        }
        return summation;
    }

    private double qualityToReturn(List<Station> stations, Station station) {
        double summation = 0;
        if (!stations.isEmpty()) {
            double factor = 0.0;
            double multiplication = 1.0;
            double maxDistance = parameters.maxdistance;
            double distance = 0.0;
            for (Station s : stations) {
                distance = station.getPosition().distanceTo(s.getPosition());
                if (maxDistance > distance) {
                    factor = (maxDistance - distance) / maxDistance;
                }
                multiplication = s.availableSlots() * factor;
                summation += multiplication;
            }
        }
        return summation;
    }

    private Station nearestStationToRent(List<Station> stations, GeoPoint point) {
        Comparator<Station> byDistance = StationComparator.byDistance(point);
        List<Station> orderedStations = stations.stream().filter(s -> s.availableBikes() > 0)
                .sorted(byDistance).collect(Collectors.toList());
        return orderedStations.get(0);
    }

    private Station nearestStationToReturn(List<Station> stations, GeoPoint point) {
        Comparator<Station> byDistance = StationComparator.byDistance(point);
        List<Station> orderedStations = stations.stream().filter(s -> s.availableSlots() > 0)
                .sorted(byDistance).collect(Collectors.toList());
        return orderedStations.get(0);
    }

}
