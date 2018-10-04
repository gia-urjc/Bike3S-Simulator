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

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
@UserType("USER_ECONOMIC_INCENTIVES")
public class UserEconomicIncentives extends User {

    @UserParameters
    public class Parameters {
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
        
        private GeoPoint destinationPlace;
        

                
        private Parameters() {}

    }

    private final int COMPENSATION = 10;   // 1 incentive unit  per 10 meters
    private final int EXTRA = 30;   // 30% of quality
    
    private Parameters parameters;
    
    public UserEconomicIncentives(Parameters parameters, SimulationServices services) {
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
        List<Station> stations = informationSystem.getStations();
        Station nearestStation = nearestStationToRent(stations, this.getPosition());

        		if(!recommendedStations.isEmpty()) {
        			int i = 0;
        			while (destination == null && i < recommendedStations.size()) {
        					Station station = recommendedStations.get(i).getStation();
        					
        					double incentive = recommendedStations.get(i).getIncentive();
        					double quality = qualityToRent(stations, station);
        					double compensation = compensation(this.getPosition(), nearestStation, station);
        					double extra = quality*EXTRA/100;
        					if (incentive >= (compensation+extra)) {
        							destination = recommendedStations.get(i).getStation();
        					}
        					System.out.println("station "+station.getId());
        					System.out.println("incentive: "+incentive);
        					System.out.println("min expected incentive: "+(compensation+extra));
            i++;
        			}
        }
        		if(destination == null) {
        			destination = nearestStation;
        		}
        return destination;
    }

    @Override
    public Station determineStationToReturnBike() {
        Station destination = null;
        List<Recommendation> recommendedStations = recommendationSystem.recommendStationToReturnBike(this.getDestinationPlace());
        //Remove station if the user is in this station
        recommendedStations.removeIf(recommendation -> recommendation.getStation().getPosition().equals(this.getPosition()));
        List<Station> stations = informationSystem.getStations();
        Station nearestStation = nearestStationToReturn(stations, this.getDestinationPlace());
        if (!recommendedStations.isEmpty()) {
									int i = 0;
									while (destination == null && i < recommendedStations.size()) {
											Station station = recommendedStations.get(i).getStation();
											double incentive = recommendedStations.get(i).getIncentive();
											double quality = qualityToReturn(stations, station);
											double compensation = compensation(this.getDestinationPlace(), nearestStation, station);
											double extra = quality*EXTRA/100;
											if (incentive >= (compensation+extra)) {
													destination = recommendedStations.get(i).getStation();
											}
											System.out.println("station "+station.getId());
											System.out.println("incentive: "+incentive);
											System.out.println("min expected incentive: "+(compensation+extra));											
						    i++;
									}
        }
        if (destination == null) {
        		destination  = nearestStation;
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
        return super.toString() + "UserDistanceRestriction{" +
                "parameters=" + parameters +
                '}';
    }

    private double compensation(GeoPoint point, Station nearestStation, Station recommendedStation) {
    	double distanceToNearestStation = nearestStation.getPosition().distanceTo(point);
    	double distanceToRecommendedStation  = recommendedStation.getPosition().distanceTo(point);
    	return (distanceToRecommendedStation - distanceToNearestStation)/COMPENSATION;
    }
    
    private double qualityToRent(List<Station> stations, Station station) {
		double summation = 0;
		if (!stations.isEmpty()) {
			double factor, multiplication;
			double maxDistance = recommendationSystem.getDistance();
			for (Station s: stations) {
				factor = (maxDistance - station.getPosition().distanceTo(s.getPosition()))/maxDistance;
				multiplication = s.availableBikes()*factor;
				summation += multiplication; 
			}
		}
		return summation;
	}
	
	private double qualityToReturn(List<Station> stations, Station station) {
		double summation = 0;
		if (!stations.isEmpty()) {
			double factor, multiplication;
			double maxDistance = recommendationSystem.getDistance();
			for (Station s: stations) {
				factor = (maxDistance - station.getPosition().distanceTo(s.getPosition()))/maxDistance;
				multiplication = s.availableSlots()*factor;
				summation += multiplication; 
			}
		}
		return summation;
	}

	private Station nearestStationToRent(List<Station> stations, GeoPoint point) {
		Comparator<Station> byDistance = services.getStationComparator().byDistance(point);
		List<Station> orderedStations = stations.stream().filter(s -> s.availableBikes() > 0)
				.sorted(byDistance).collect(Collectors.toList());
		return orderedStations.get(0);
	}
	
	private Station nearestStationToReturn(List<Station> stations, GeoPoint point) {
		Comparator<Station> byDistance = services.getStationComparator().byDistance(point);
		List<Station> orderedStations = stations.stream().filter(s -> s.availableSlots() > 0)
				.sorted(byDistance).collect(Collectors.toList());
		return orderedStations.get(0);
	}


}
