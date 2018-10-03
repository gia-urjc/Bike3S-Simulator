package es.urjc.ia.bikesurbanfleets.users.types;

import es.urjc.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoRoute;
import es.urjc.ia.bikesurbanfleets.common.util.SimulationRandom;
import es.urjc.ia.bikesurbanfleets.consultSystems.recommendationSystemTypes.Recommendation;
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

        /**
         *  User destination place
         */
        private GeoPoint destinationPlace;

        /**
         * It is the number of times that the user musts try to make a bike reservation before
         * deciding to leave the system.
         */
        private int minReservationAttempts = infraestructure.getRandom().nextInt(3, 7);

        /**
         * It is the number of times that a reservation timeout event musts occurs before the
         * user decides to leave the system.
         */
        private int minReservationTimeouts = infraestructure.getRandom().nextInt(2, 5);

        /**
         * It is the number of times that the user musts try to rent a bike (without a bike
         * reservation) before deciding to leave the system.
         */
        private int minRentalAttempts = infraestructure.getRandom().nextInt(4, 8);

        /**
         * It determines the rate with which the user will decide to go directly to a station
         * in order to return the bike he has just rented.
         */
        private int bikeReturnPercentage;

        /**
         * It determines the rate with which the user will choose a new destination station
         * after a  timeout event happens.
         */
        private int reservationTimeoutPercentage;

        /**
         * It determines the rate with which the user will choose a new destination station
         * after he hasn't been able to make a reservation.
         */
        private int failedReservationPercentage;

        
        /**
         * It determines if the user will make a reservation or not.
         */
        private boolean willReserve;
                
        private Parameters() {}

    }

    private Parameters parameters;
    
    public UserObedient(Parameters parameters, SimulationServices services) {
        super(services, parameters.destinationPlace);
        this.parameters = parameters;
    }
    
    @Override
    public boolean decidesToLeaveSystemAfterTimeout() {
        return getMemory().getReservationTimeoutsCounter() == parameters.minReservationTimeouts ? true : false;
    }

    @Override
    public boolean decidesToLeaveSystemAffterFailedReservation() {
        return getMemory().getReservationAttemptsCounter() == parameters.minReservationAttempts ? true : false;
    }

    @Override
    public boolean decidesToLeaveSystemWhenBikesUnavailable() {
        return getMemory().getRentalAttemptsCounter() == parameters.minRentalAttempts ? true : false;
    }
    
    @Override
    public Station determineStationToRentBike() {
        Station destination = null;
        List<Recommendation> recommendedStations = recommendationSystem.recommendStationToRentBike(this.getPosition());
        //Remove station if the user is in this station
        recommendedStations.removeIf(recommendation -> recommendation.getStation().getPosition().equals(this.getPosition()) && recommendation.getStation().availableBikes() == 0);
        if (!recommendedStations.isEmpty()) {
            destination = recommendedStations.get(0).getStation();
        }
        return destination;
    }

    @Override
    public Station determineStationToReturnBike() {
        Station destination = null;
                
        if(destinationPlace == null) {
            SimulationRandom random = SimulationRandom.getGeneralInstance();
            destinationPlace = this.infraestructure.generateBoundingBoxRandomPoint(random);
        }
        
        List<Recommendation> recommendedStations = recommendationSystem.recommendStationToReturnBike(destinationPlace);
        //Remove station if the user is in this station
        recommendedStations.removeIf(recommendation -> recommendation.getStation().getPosition().equals(this.getPosition()));
        if (!recommendedStations.isEmpty()) {
        	destination = recommendedStations.get(0).getStation();
        }
        return destination;
    }
    
    @Override
    public boolean decidesToReserveBikeAtSameStationAfterTimeout() {
        return parameters.willReserve;
    }

    @Override
    public boolean decidesToReserveBikeAtNewDecidedStation() {
    	return parameters.willReserve;
    }

    @Override
    public boolean decidesToReserveSlotAtSameStationAfterTimeout() {
    	return parameters.willReserve;
    }

    @Override
    public boolean decidesToReserveSlotAtNewDecidedStation() {
    	return parameters.willReserve;
    }

    @Override
    public GeoPoint decidesNextPoint() {
        return infraestructure.generateBoundingBoxRandomPoint(SimulationRandom.getGeneralInstance());
    }

    @Override
    public boolean decidesToReturnBike() {
        int percentage = infraestructure.getRandom().nextInt(0, 100);
        return percentage < parameters.bikeReturnPercentage ? true : false;
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterTimeout() {
        int percentage = infraestructure.getRandom().nextInt(0, 100);
        return percentage < parameters.reservationTimeoutPercentage ? true : false;
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterFailedReservation() {
        int percentage = infraestructure.getRandom().nextInt(0, 100);
        return percentage < parameters.failedReservationPercentage ? true : false;
    }
    
    @Override
    public GeoRoute determineRoute() throws Exception {
        List<GeoRoute> routes = null;
        routes = calculateRoutes(getDestinationPoint());
        return routes != null ? routes.get(0) : null;
    }

    @Override
    public String toString() {
        return super.toString() + "UserObedient{" +
                "parameters=" + parameters +
                '}';
    }

}
