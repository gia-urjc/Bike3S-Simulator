/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.worldentities.users.types;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.worldentities.users.User;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecision;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecisionGoToPointInCity;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecisionLeaveSystem;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecisionReserveBike;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecisionReserveSlot;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserType;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author holger
 */
@UserType("USER_UNINFORMED_RES")
public class UserUninformedReservation extends User {

    @Override
    public UserDecision decideAfterAppearning() {
        Station s = determineStationToRentBike();        
        if (s != null) { //user has found a station
            double dist=s.getPosition().distanceTo(this.getPosition());
            if (dist<= parameters.maxDistanceToRentBike-getMemory().getWalkedToTakeBikeDistance()) {
                return new UserDecisionReserveBike(s);
            }
        } //if not he would leave
        return new UserDecisionLeaveSystem();
    }

    @Override
    public UserDecision decideAfterFailedRental() {
//        if (getMemory().getRentalAttemptsCounter() >= parameters.minRentalAttempts ||
//                getMemory().getReservationAttemptsCounter() >= parameters.minRentalAttempts) {
//            return new UserDecisionLeaveSystem();
//        } else {
        Station s = determineStationToRentBike();
        if (s != null) { //user has found a station
            double dist=s.getPosition().distanceTo(this.getPosition());
            if (dist<= parameters.maxDistanceToRentBike-getMemory().getWalkedToTakeBikeDistance()) {
                return new UserDecisionReserveBike(s);
            }
        } //if not he would leave
        return new UserDecisionLeaveSystem();
    }

    //no reservations will take place
    @Override
    public UserDecision decideAfterFailedBikeReservation() {
//        if (getMemory().getRentalAttemptsCounter() >= parameters.minRentalAttempts ||
//                getMemory().getReservationAttemptsCounter() >= parameters.minRentalAttempts) {
//            return new UserDecisionLeaveSystem();
//        } else {
        Station s = determineStationToRentBike();
        if (s != null) { //user has found a station
            double dist=s.getPosition().distanceTo(this.getPosition());
            if (dist<= parameters.maxDistanceToRentBike-getMemory().getWalkedToTakeBikeDistance()) {
                return new UserDecisionReserveBike(s);
            }
        } //if not he would leave
        return new UserDecisionLeaveSystem();
    }
    //TODO: change the implementation of this decision
    @Override
    public UserDecision decideAfterBikeReservationTimeout() {
        Station s = this.getDestinationStation();
        if (s != null) { //user has found a station
            double dist=s.getPosition().distanceTo(this.getPosition());
            if (dist<= parameters.maxDistanceToRentBike-getMemory().getWalkedToTakeBikeDistance()) {
                return new UserDecisionReserveBike(s);
            }
        } //if not he would leave
        return new UserDecisionLeaveSystem();
     }

    @Override
    public UserDecision decideAfterGettingBike() {
        if (intermediatePosition != null) {
            return new UserDecisionGoToPointInCity(intermediatePosition);
        } else {
            Station s = determineStationToReturnBike();
            return new UserDecisionReserveSlot(s);
        }
    }

    @Override
    public UserDecision decideAfterFailedReturn() {
        Station s = determineStationToReturnBike();
        return new UserDecisionReserveSlot(s);
    }

    @Override
    public UserDecision decideAfterFinishingRide() {
        Station s = determineStationToReturnBike();
        return new UserDecisionReserveSlot(s);
    }

    @Override
    public UserDecision decideAfterFailedSlotReservation() {
        Station s = determineStationToReturnBike();
        return new UserDecisionReserveSlot(s);
    }

    @Override
    public UserDecision decideAfterSlotReservationTimeout() {
            Station s = this.getDestinationStation();
            if (s != null) { //user has found a station
                return new UserDecisionReserveSlot(s);
            } else {
                throw new RuntimeException("User " + this.getId() + " cant return a bike, no slots");
            }
    }

    public class Parameters {

       //default constructor used if no parameters are specified
        private Parameters() {}
        
        /**
         * It is the number of times that the user will try to rent a bike (without a bike
         * reservation) before deciding to leave the system.
         */
 //        int minRentalAttempts = 3;

         int maxDistanceToRentBike = 600;

    }

    Parameters parameters;

    public UserUninformedReservation(JsonObject userdef, SimulationServices services, long seed) throws Exception {
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
        List<Station> triedStations = getMemory().getStationsWithReservationRentalFailedAttempts(); 

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
        List<Station> triedStations = getMemory().getStationsWithReservationReturnFailedAttempts(); 

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
