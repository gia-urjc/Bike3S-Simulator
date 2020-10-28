/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.worldentities.users.types;

import com.google.gson.JsonObject;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.services.Recommendation;
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
            return new UserDecisionReserveBike(s);
        } //station has not been found           
        return new UserDecisionLeaveSystem();
    }

    @Override
    public UserDecision decideAfterFailedRental() {
        return decideAfterAppearning();
    }

    @Override
    public UserDecision decideAfterFailedBikeReservation() {
        return decideAfterAppearning();
    }

    @Override
    public UserDecision decideAfterBikeReservationTimeout() {
        Station s = this.getDestinationStation();
        if (s != null) {
            return new UserDecisionReserveBike(s);
        } else {
            throw new RuntimeException("User " + this.getId() + " imposible state after timeout");
        }
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
            throw new RuntimeException("User " + this.getId() + " imposible state after timeout");
        }
    }

    public class Parameters {

        //default constructor used if no parameters are specified
        private Parameters() {
        }

        /**
         * It is the number of times that the user will try to rent a bike
         * (without a bike reservation) before deciding to leave the system.
         */
        //        int minRentalAttempts = 3;
        double maxDistanceToRentBike = 600;

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
        double desiredmaxdistance = Math.max(0, parameters.maxDistanceToRentBike - getMemory().getWalkedToTakeBikeDistance());

        List<Recommendation> stations = informationSystem.getStationsOrderedByWalkDistanceWithinMaxDistance(this.getPosition(), desiredmaxdistance);
        UserUninformed.removeTriedStations(stations, getMemory().getStationsWithReservationRentalFailedAttempts());
 
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
        UserUninformed.removeTriedStations(stations, getMemory().getStationsWithReservationReturnFailedAttempts());
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
