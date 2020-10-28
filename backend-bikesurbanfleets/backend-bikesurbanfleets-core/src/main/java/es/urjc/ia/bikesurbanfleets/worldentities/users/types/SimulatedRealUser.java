package es.urjc.ia.bikesurbanfleets.worldentities.users.types;

import com.google.gson.JsonObject;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.services.Recommendation;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import es.urjc.ia.bikesurbanfleets.worldentities.users.User;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecision;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecisionGoToPointInCity;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecisionGoToStation;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecisionLeaveSystem;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserParameters;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserType;
import java.util.List;

/**
 *
 * @author holger
 */
@UserType("USER_SIMULATED")
public class SimulatedRealUser extends User {

    @Override
    public UserDecision decideAfterAppearning() {
        Station s = determineStationToRentBike();
        if (s != null) { //user has found a station
            return new UserDecisionGoToStation(s);
        } //if not he would leave
        return new UserDecisionLeaveSystem();
    }

    @Override
    public UserDecision decideAfterFailedRental() {
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
        }else {
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
    }

    Parameters parameters;

    public SimulatedRealUser(JsonObject userdef, SimulationServices services, long seed) throws Exception {
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
        List<Recommendation> stations = informationSystem.getAllStationsOrderedByWalkDistance(this.getPosition());
        if (printHints) {
            informationSystem.printRecomendations(stations, 100000, true, maxNumberRecommendationPrint);
        }
        if (!stations.isEmpty()) {
            return stations.get(0).getStation();
        }
        return null;
    }

    @Override
    protected Station determineStationToReturnBike() {
        List<Recommendation> stations = informationSystem.getAllStationsOrderedByWalkDistance(destinationPlace);
        UserUninformed.removeTriedStations(stations, getMemory().getStationsWithReturnFailedAttempts());

        if (printHints) {
            informationSystem.printRecomendations(stations, 0, false, maxNumberRecommendationPrint);
        }
        if (!stations.isEmpty()) {
            return stations.get(0).getStation();
        } else {
            throw new RuntimeException("[Error] User " + this.getId() + " cant return a bike, all stations tried");
        }
    }
}
