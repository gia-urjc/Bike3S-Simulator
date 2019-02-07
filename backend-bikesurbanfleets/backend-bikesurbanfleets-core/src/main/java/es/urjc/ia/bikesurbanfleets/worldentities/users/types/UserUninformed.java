package es.urjc.ia.bikesurbanfleets.worldentities.users.types;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.core.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserParameters;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserType;
import es.urjc.ia.bikesurbanfleets.worldentities.users.User;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecision;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecisionGoToPointInCity;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecisionLeaveSystem;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecisionStation;

import java.util.List;
import java.util.stream.Collectors;

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
            return new UserDecisionStation(s, false);
        } else {
            return new UserDecisionLeaveSystem();
        }
    }

    @Override
    public UserDecision decideAfterFailedRental() {
        if (getMemory().getRentalAttemptsCounter() >= parameters.minRentalAttempts) {
            return new UserDecisionLeaveSystem();
        } else {
            Station s = determineStationToRentBike();
            if (s != null) { //user has found a station
                return new UserDecisionStation(s, false);
            } else {
                return new UserDecisionLeaveSystem();
            }
        }
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
        if (parameters.intermediatePosition != null) {
            return new UserDecisionGoToPointInCity(parameters.intermediatePosition);
        } else {
            Station s = determineStationToReturnBike();
            if (s != null) { //user has found a station
                return new UserDecisionStation(s, false);
            } else {
                throw new RuntimeException("user cant return a bike, no slots");
            }
        }
    }

    @Override
    public UserDecisionStation decideAfterFailedReturn() {
        Station s = determineStationToReturnBike();
        return new UserDecisionStation(s, false);
    }

    @Override
    public UserDecisionStation decideAfterFinishingRide() {
        Station s = determineStationToReturnBike();
        return new UserDecisionStation(s, false);
    }

    @Override
    public UserDecisionStation decideAfterFailedSlotReservation() {
        return null;
    }
    //TODO: should this method appear in User class?  
    @Override
    public UserDecisionStation decideAfterSlotReservationTimeout() {
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
        int minRentalAttempts = 3;

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

        Station destination = null;
        List<Station> triedStations = getMemory().getStationsWithRentalFailedAttempts();
        List<Station> finalStations = informationSystem.getAllStationsOrderedByDistance(this.getPosition()).stream()
                .filter(station -> station.getPosition().distanceTo(this.getPosition()) <= parameters.maxDistanceToRentBike).collect(Collectors.toList());
        finalStations.removeAll(triedStations);

        if (!finalStations.isEmpty()) {
            destination = finalStations.get(0);
        }
        return destination;
    }

    @Override
    protected Station determineStationToReturnBike() {
        Station destination = null;
        List<Station> triedStations = getMemory().getStationsWithReturnFailedAttempts();
        List<Station> finalStations = informationSystem.getAllStationsOrderedByDistance(this.destinationPlace);
        finalStations.removeAll(triedStations);
        if (!finalStations.isEmpty()) {
            destination = finalStations.get(0);
        } else {
            throw new RuntimeException("user cant return a bike, no slots");
        }
        return destination;
    }
}
