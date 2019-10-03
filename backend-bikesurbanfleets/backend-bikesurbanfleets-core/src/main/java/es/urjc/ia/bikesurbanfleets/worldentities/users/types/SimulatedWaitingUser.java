/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.worldentities.users.types;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import es.urjc.ia.bikesurbanfleets.worldentities.users.User;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecision;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecisionGoToPointInCity;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecisionGoToStation;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecisionLeaveSystem;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecisionReserveBike;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserParameters;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserType;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * this user does no reservations and just waits if no sucess
 */
@UserType("USER_SIMULATEDWAITING")
public class SimulatedWaitingUser extends User {

    private Station laststation = null;
    private int waitingstart = -1;

    @Override
    public UserDecision decideAfterAppearning() {
        Station s = determineStationToRentBike();
        if (s != null) { //user has found a station
            laststation = s;
            return new UserDecisionGoToStation(s);
        } //if not he would leave
        return new UserDecisionLeaveSystem();
    }

    @Override
    public UserDecision decideAfterFailedRental() {
        if (waitingstart == -1) {
            waitingstart = (int) SimulationDateTime.getCurrentSimulationInstant();
        }
        if ((SimulationDateTime.getCurrentSimulationInstant() - waitingstart) > parameters.MaxWaitingTime) {
            return new UserDecisionLeaveSystem();
        } else {
            return new UserDecisionGoToStation(laststation);
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
        waitingstart = -1;
        if (intermediatePosition != null) {
            return new UserDecisionGoToPointInCity(intermediatePosition);
        } else {
            Station s = determineStationToReturnBike();
            laststation = s;
            return new UserDecisionGoToStation(s);
        }
    }

    @Override
    public UserDecision decideAfterFailedReturn() {
        if (waitingstart == -1) {
            waitingstart = (int) SimulationDateTime.getCurrentSimulationInstant();
        }
        if ((SimulationDateTime.getCurrentSimulationInstant() - waitingstart) > parameters.MaxWaitingTime) {
            Station s = determineStationToReturnBike();
            laststation = s;
            waitingstart = -1;
            return new UserDecisionGoToStation(s);
        } else {
            return new UserDecisionGoToStation(laststation);
        }
    }

    @Override
    public UserDecision decideAfterFinishingRide() {
        Station s = determineStationToReturnBike();
        laststation = s;
        waitingstart = -1;
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
        int MaxWaitingTime = 600;

    }

    Parameters parameters;

    public SimulatedWaitingUser(JsonObject userdef, SimulationServices services, long seed) throws Exception {
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
        List<Station> finalStations = informationSystem.getAllStationsOrderedByDistance(this.getPosition()).stream().collect(Collectors.toList());

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
            throw new RuntimeException("[Error] User " + this.getId() + " cant return a bike, no slots");
        }
        return destination;
    }
}
