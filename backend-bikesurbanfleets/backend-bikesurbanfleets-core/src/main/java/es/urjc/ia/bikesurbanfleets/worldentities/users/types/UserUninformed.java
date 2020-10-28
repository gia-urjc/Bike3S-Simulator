package es.urjc.ia.bikesurbanfleets.worldentities.users.types;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.services.Recommendation;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserParameters;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserType;
import es.urjc.ia.bikesurbanfleets.worldentities.users.User;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecision;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecisionGoToPointInCity;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecisionGoToStation;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecisionLeaveSystem;

import java.util.List;
import java.util.function.Predicate;

/**
 * This class represents a user who doesn't know anything about the state of the
 * system. This user always chooses the closest destination station and the
 * shortest route to reach it. This user decides to leave the system randomly
 * when a reservation fails if reservations are active
 *
 * @author IAgroup
 *
 */
@UserType("USER_UNINFORMED")
public class UserUninformed extends User {

    @Override
    public UserDecision decideAfterAppearning() {
        Station s = determineStationToRentBike();
        if (s != null) { //user has found a station
            return new UserDecisionGoToStation(s);
        } //else  station has not been found           
        if (printHints) {
            System.out.format("[UserInfo] User: %d abandons after appearing. No station found within acceptable distance. Has walked already %f meters of %f maximum.%n", this.getId(),
                    getMemory().getWalkedToTakeBikeDistance(), parameters.maxDistanceToRentBike);
        }
        return new UserDecisionLeaveSystem();
    }

    @Override
    public UserDecision decideAfterFailedRental() {
        Station s = determineStationToRentBike();
        if (s != null) { //user has found a station
            return new UserDecisionGoToStation(s);
        } //else  station has not been found           
        if (printHints) {
            System.out.format("[UserInfo] User: %d abandons after failed rental. No station found within acceptable distance. Has walked already %f meters of %f maximum.%n", this.getId(),
                    getMemory().getWalkedToTakeBikeDistance(), parameters.maxDistanceToRentBike);
        }
        return new UserDecisionLeaveSystem();
    }

    //no reservations will take place
    @Override
    public UserDecision decideAfterFailedBikeReservation() {
        return null;
    }

    @Override
    public UserDecision decideAfterBikeReservationTimeout() {
        return null;
    }

    @Override
    public UserDecision decideAfterGettingBike() {
        if (intermediatePosition != null) {
            return new UserDecisionGoToPointInCity(intermediatePosition);
        } else {
            Station s = determineStationToReturnBike();
            return new UserDecisionGoToStation(s);
        }
    }

    @Override
    public UserDecision decideAfterFailedReturn() {
        Station s = determineStationToReturnBike();
        return new UserDecisionGoToStation(s);
    }

    @Override
    public UserDecision decideAfterFinishingRide() {
        Station s = determineStationToReturnBike();
        return new UserDecisionGoToStation(s);
    }

    @Override
    public UserDecision decideAfterFailedSlotReservation() {
        return null;
    }

    //TODO: should this method appear in User class?  
    @Override
    public UserDecision decideAfterSlotReservationTimeout() {
        return null;
    }

    @UserParameters
    public class Parameters {

        //default constructor used if no parameters are specified
        private Parameters() {
        }

        /**
         * It is the number of times that the user will try to rent a bike
         * (without a bike reservation) before deciding to leave the system.
         */
        //      int minRentalAttempts = 3;
        double maxDistanceToRentBike = 600;
    }

    Parameters parameters;

    public UserUninformed(JsonObject userdef, SimulationServices services, long seed) throws Exception {
        super(services, userdef, seed);
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
    protected Station determineStationToRentBike() {
        double desiredmaxdistance = Math.max(0, parameters.maxDistanceToRentBike - getMemory().getWalkedToTakeBikeDistance());

        List<Recommendation> stations = informationSystem.getStationsOrderedByWalkDistanceWithinMaxDistance(this.getPosition(), desiredmaxdistance);
        removeTriedStations(stations, getMemory().getStationsWithRentalFailedAttempts());

        if (printHints) {
            informationSystem.printRecomendations(stations, desiredmaxdistance, true, maxNumberRecommendationPrint);
        }
        if (!stations.isEmpty()) {
            return stations.get(0).getStation();
        }
        return null;
    }

    @Override
    protected Station determineStationToReturnBike() {
        List<Recommendation> stations = informationSystem.getAllStationsOrderedByWalkDistance(destinationPlace);
        removeTriedStations(stations, getMemory().getStationsWithReturnFailedAttempts());

        if (printHints) {
            informationSystem.printRecomendations(stations, 0, false, maxNumberRecommendationPrint);
        }

        if (!stations.isEmpty()) {
            return stations.get(0).getStation();
        } else {
            throw new RuntimeException("[Error] User " + this.getId() + " cant return a bike, all stations tried");
        }
    }

    final static void removeTriedStations(List<Recommendation> rec, List<Station> tried) {

        Predicate<Recommendation> pr;
        if (tried.size() > 0) {
            for (Station stried : tried) {
                pr = p -> p.getStation() == stried;
                rec.removeIf(pr);
            }
        }
    }

}
