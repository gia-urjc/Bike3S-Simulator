package es.urjc.ia.bikesurbanfleets.worldentities.users.types;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserParameters;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserType;
import es.urjc.ia.bikesurbanfleets.worldentities.users.User;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecision;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecisionGoToPointInCity;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecisionGoToStation;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecisionLeaveSystem;

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

    boolean printHints = false;
    static double ratio=0;
    static int rationumber=0;
    double lastdist=0;
    double lastwalked=0;
    @Override
    public UserDecision decideAfterAppearning() {
        Station s = determineStationToRentBike();
        if (s != null) { //user has found a station
            double dist=routeService.estimateDistance(this.getPosition(), s.getPosition(), "foot");
            if (dist <= parameters.maxDistanceToRentBike - getMemory().getWalkedToTakeBikeDistance()) {
                lastdist=dist;
                lastwalked=getMemory().getWalkedToTakeBikeDistance();
                return new UserDecisionGoToStation(s);
            }
            if (printHints) {
                System.out.format("[UserInfo] User: %d abandons after appearing. Station at distance %f found. But has walked %f meters of %f maximum.%n", this.getId(),
                        dist, getMemory().getWalkedToTakeBikeDistance(), parameters.maxDistanceToRentBike);
            }
            return new UserDecisionLeaveSystem();
        } //if not he would leave
        if (printHints) {
            System.out.format("[UserInfo] User: %d abandons after appearing. No station found. Has walked %f meters of %f maximum.%n", this.getId(),
                    getMemory().getWalkedToTakeBikeDistance(), parameters.maxDistanceToRentBike);
        }
        return new UserDecisionLeaveSystem();
    }

    @Override
    public UserDecision decideAfterFailedRental() {
//        if (getMemory().getRentalAttemptsCounter() >= parameters.minRentalAttempts) {
//            return new UserDecisionLeaveSystem();
//        } else {
            if (printHints) {
                UserInformed.ratio=(UserInformed.ratio * UserInformed.rationumber)
                        + (getMemory().getWalkedToTakeBikeDistance() - lastwalked)/lastdist;
                UserInformed.rationumber++;
                UserInformed.ratio=UserInformed.ratio/UserInformed.rationumber;               
                System.out.format("[UserInfo] Total ratio real/estimated distance: %f. %n", UserInformed.ratio);
                System.out.format("[UserInfo] User: %d after failed rental. Estimated distance %f. Real distance %f. Ratio %f.%n", this.getId(),
                    lastdist, getMemory().getWalkedToTakeBikeDistance() - lastwalked,   (getMemory().getWalkedToTakeBikeDistance() - lastwalked)/lastdist);
            }
        Station s = determineStationToRentBike();
        if (s != null) { //user has found a station
            double dist=routeService.estimateDistance(this.getPosition(), s.getPosition(), "foot");
            if (dist <= parameters.maxDistanceToRentBike - getMemory().getWalkedToTakeBikeDistance()) {
                lastdist=dist;
                lastwalked=getMemory().getWalkedToTakeBikeDistance();
                return new UserDecisionGoToStation(s);
            }
            if (printHints) {
                System.out.format("[UserInfo] User: %d abandons after failed rental. Station at distance %f found. But has walked %f meters of %f maximum.%n", this.getId(),
                        dist, getMemory().getWalkedToTakeBikeDistance(), parameters.maxDistanceToRentBike);
            }
            return new UserDecisionLeaveSystem();
        } //if not he would leave
        if (printHints) {
            System.out.format("[UserInfo] User: %d abandons after failed rental. No station found. Has walked %f meters of %f maximum.%n", this.getId(),
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
            if (printHints) {
                UserInformed.ratio=(UserInformed.ratio * UserInformed.rationumber)
                        + (getMemory().getWalkedToTakeBikeDistance() - lastwalked)/lastdist;
                UserInformed.rationumber++;
                UserInformed.ratio=UserInformed.ratio/UserInformed.rationumber;               
                System.out.format("[UserInfo] Total ratio real/estimated distance: %f. %n", UserInformed.ratio);
                System.out.format("[UserInfo] User: %d after sucessful rental. Estimated distance %f. Real distance %f. Ratio %f.%n", this.getId(),
                    lastdist, getMemory().getWalkedToTakeBikeDistance() - lastwalked,   (getMemory().getWalkedToTakeBikeDistance() - lastwalked)/lastdist);
            }
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

        Station destination = null;
        List<Station> triedStations = getMemory().getStationsWithRentalFailedAttempts();
        //double walkeddistance=
        List<Station> finalStations = informationSystem.getAllStationsOrderedByDistance(this.getPosition(),"foot");
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
        List<Station> finalStations = informationSystem.getAllStationsOrderedByDistance(this.destinationPlace,"foot");
        finalStations.removeAll(triedStations);
        if (!finalStations.isEmpty()) {
            destination = finalStations.get(0);
        } else {
            throw new RuntimeException("[Error] User " + this.getId() + " cant return a bike, no slots");
        }
        return destination;
    }
}
