package es.urjc.ia.bikesurbanfleets.users.types;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.users.UserParameters;
import es.urjc.ia.bikesurbanfleets.users.UserType;
import es.urjc.ia.bikesurbanfleets.users.User;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class represents a user whose behaviour is the same of UserReasonable with the 
 * exception that this user doesn't accept recommended stations which are farer that a 
 * certain distance. 
 * Then, if there are no stations to rent a bike which are nearer than the specified 
 * distance, he'll leave the system.
 * If there aren't any stations to return the bike nearer than the restrictive distance,
 * the user will go to the closest station.  
 * 
 * @author IAgroup
 *
 */
@UserType("USER_DISTANCE_RESTRICTION")
public class UserDistanceRestriction extends User {

     @UserParameters
    public class Parameters {

 
        //default constructor used if no parameters are specified
        private Parameters() {}
        /**
         * It is the number of times that the user musts try to rent a bike (without a bike
         * reservation) before deciding to leave the system.
         */
        private int minRentalAttempts = 2;

        /**
         * It is a distance restriction: this user dosn't go to destination stations which are
         * farer than this distance.
         */
        private int maxDistance=300;

        @Override
        public String toString() {
            return "UserDistanceRestrictionParameters{" +
                    ", minRentalAttempts=" + minRentalAttempts +
                    ", maxDistance=" + maxDistance +
                    '}';
        }
    }

    private Parameters parameters;

    public UserDistanceRestriction(JsonObject userdef, SimulationServices services, long seed) throws Exception{
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

    
    //**********************************************
    //Decision related to reservations
    @Override
    public boolean decidesToLeaveSystemAfterTimeout() {
        return false;
    }
    @Override
    public boolean decidesToLeaveSystemAffterFailedReservation() {
        return false;
    }
    @Override
    public boolean decidesToReserveBikeAtSameStationAfterTimeout() {
        return false;
    }

    @Override
    public boolean decidesToReserveBikeAtNewDecidedStation() {
        return false;
    }

    @Override
    public boolean decidesToReserveSlotAtSameStationAfterTimeout() {
        return false;
    }

    @Override
    public boolean decidesToReserveSlotAtNewDecidedStation() {
        return false;
    }
    @Override
    public boolean decidesToDetermineOtherStationAfterTimeout() {
        return false;
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterFailedReservation() {
        return false;
    }

    //**********************************************
    //decisions related to taking and leaving a bike
    @Override
    public boolean decidesToLeaveSystemWhenBikesUnavailable() {
        if (getMemory().getRentalAttemptsCounter() >= parameters.minRentalAttempts) 
            return true; 
        else return false;
     }


    @Override
    public Station determineStationToRentBike() {
        Station destination = null;
        List<Station> recommendedStations = recommendationSystem.recommendStationToRentBike(this.getPosition())
        		.stream().map((recommendation -> recommendation.getStation()))
        		.filter(station -> station.getPosition().distanceTo(this.getPosition()) <= parameters.maxDistance)
        		.collect(Collectors.toList());
        if (!recommendedStations.isEmpty()) {
            destination = recommendedStations.get(0);
        }
        return destination;
    }

    @Override
     public Station determineStationToReturnBike() {
        Station destination = null;
        List<Station> recommendedStations = recommendationSystem.recommendStationToReturnBike(destinationPlace)
        		.stream().map((recommendation -> recommendation.getStation()))
        		.collect(Collectors.toList());
        //Remove station if the user is in this station
       recommendedStations.removeIf(station -> station.getPosition().equals(this.getPosition()));
        if (!recommendedStations.isEmpty()) {
        	destination = recommendedStations.get(0);
        }
        return destination;
    }
    
    @Override
    public String toString() {
        return super.toString() + "UserDistanceRestriction{" +
                "parameters=" + parameters +
                '}';
    }
    
        //**********************************************
    //decisions related to either go directly to the destination or going arround

    @Override
    public boolean decidesToGoToPointInCity() {
        return false;
    }

    @Override
    public GeoPoint getPointInCity() {
        return null;
    }

}
