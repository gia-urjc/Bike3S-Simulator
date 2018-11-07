package es.urjc.ia.bikesurbanfleets.users.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.consultSystems.recommendationSystems.Recommendation;

import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;

import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.users.UserParameters;
import es.urjc.ia.bikesurbanfleets.users.UserType;
import es.urjc.ia.bikesurbanfleets.users.User;

import java.util.List;

/**
 * This class represents a user who always follows the first system recommendations i. e., that 
 * which consist of renting a bike at the station which has more available bikes and returning 
 * the bike at the station which has more available slots. 
 * This user never reserves neither bikes nor slots at destination stations as he knows that the 
 * system is recommending him that station because it is the station which has more available 
 * bikes or slots, so he knows that, almost certainly, he'll be able to rent or to return a bike. 
  * Moreover, he always chooses the shortest routes to get his destination.
 * 
 * @author IAgroup
 */
@UserType("USER_OBEDIENT")
public class UserObedient extends User {

    @UserParameters
    public class Parameters {

        //default constructor used if no parameters are specified
        private Parameters() {}
        
        /**
         * It is the number of times that the user will try to rent a bike (without a bike
         * reservation) before deciding to leave the system.
         */
        private int minRentalAttempts = 2;


        @Override
        public String toString() {
            return "Parameters{" +
                    "minRentalAttempts=" + minRentalAttempts+
            '}';
        }

    }

    private Parameters parameters;
    
     public UserObedient(JsonObject userdef, SimulationServices services, long seed) throws Exception{
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
    public boolean decidesToLeaveSystemAfterTimeout() {
        return false;
    }

    @Override
    public boolean decidesToLeaveSystemAffterFailedReservation() {
        return false;
    }

    @Override
    public boolean decidesToLeaveSystemWhenBikesUnavailable() {
        return getMemory().getRentalAttemptsCounter() >= parameters.minRentalAttempts ? true : false;
    }
    
    @Override
    public Station determineStationToRentBike() {
        Station destination = null;
        List<Recommendation> recommendedStations = recommendationSystem.recommendStationToRentBike(this.getPosition());
        if (!recommendedStations.isEmpty()) {
            destination = recommendedStations.get(0).getStation();
        }
        return destination;
    }

    @Override
    public Station determineStationToReturnBike() {
        Station destination = null;
               
        List<Recommendation> recommendedStations = recommendationSystem.recommendStationToReturnBike(destinationPlace);
        if (!recommendedStations.isEmpty()) {
        	destination = recommendedStations.get(0).getStation();
        }
        return destination;
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
    
    @Override
    public String toString() {
        return super.toString() + "UserObedient{" +
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
