package es.urjc.ia.bikesurbanfleets.users.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.comparators.StationComparator;
import es.urjc.ia.bikesurbanfleets.consultSystems.recommendationSystems.Recommendation;
import es.urjc.ia.bikesurbanfleets.consultSystems.recommendationSystems.incentives.Incentive;
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

        //default constructor used if no parameters are specified
        private Parameters() {}
        /**
         * It is the number of times that the user will try to rent a bike (without a bike
         * reservation) before deciding to leave the system.
         */
        private int minRentalAttempts = 2;
        private  int COMPENSATION = 10;   // 1 incentive unit  per 10 meters
        private  int EXTRA = 30;   // 30% of quality
        private int maxdistance=800; //800 meters
  
        @Override
        public String toString() {
            return "Parameters{" +
                    "minRentalAttempts=" + minRentalAttempts+
            '}';
        }

    }

     private Parameters parameters;
    
    public UserEconomicIncentives(JsonObject userdef, SimulationServices services, long seed) throws Exception{
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

    @Override
    public boolean decidesToLeaveSystemWhenBikesUnavailable() {
        return getMemory().getRentalAttemptsCounter() >= parameters.minRentalAttempts ? true : false;
    }
    
    @Override
    public Station determineStationToRentBike() {
        Station destination = null;
        List<Recommendation> recommendedStations = recommendationSystem.recommendStationToRentBike(this.getPosition());
        List<Station> stations = informationSystem.getStations();
        Station nearestStation = nearestStationToRent(stations, this.getPosition());

        		if(!recommendedStations.isEmpty()) {
        			int i = 0;
        			while (destination == null && i < recommendedStations.size()) {
        					Station station = recommendedStations.get(i).getStation();
        					
        					Incentive incentive = recommendedStations.get(i).getIncentive();
        					double quality = qualityToRent(stations, station);
        					double compensation = compensation(this.getPosition(), nearestStation, station);
        					double extra = quality*parameters.EXTRA/100;
        					if (incentive.getValue() >= (int)(compensation+extra)) {
        							destination = recommendedStations.get(i).getStation();
        					}
        					System.out.println("station "+station.getId());
        					System.out.println("incentive: "+incentive.getValue());
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
                Incentive<Integer> incentive = recommendedStations.get(i).getIncentive();
                double quality = qualityToReturn(stations, station);
                double compensation = compensation(this.getDestinationPlace(), nearestStation, station);
                double extra = quality*parameters.EXTRA/100;
                if (incentive.getValue() >= (int)(compensation+extra)) {
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

    @Override
    public String toString() {
        return super.toString() + "UserDistanceRestriction{" +
                "parameters=" + parameters +
                '}';
    }

    private double compensation(GeoPoint point, Station nearestStation, Station recommendedStation) {
    	double distanceToNearestStation = nearestStation.getPosition().distanceTo(point);
    	double distanceToRecommendedStation = recommendedStation.getPosition().distanceTo(point);
    	return (distanceToRecommendedStation - distanceToNearestStation)/parameters.COMPENSATION;
    }
    
    private double qualityToRent(List<Station> stations, Station station) {
		double summation = 0;
		if (!stations.isEmpty()) {
			double factor = 0.0;
			double multiplication = 1.0;
			double maxDistance = parameters.maxdistance;
			double distance = 0.0;
			for (Station s: stations) {
				distance = station.getPosition().distanceTo(s.getPosition());
				if (maxDistance > distance) {
					factor = (maxDistance - distance)/maxDistance;
				}
				multiplication = s.availableBikes()*factor;
				summation += multiplication; 
			}
		}
		return summation;
	}
	
	private double qualityToReturn(List<Station> stations, Station station) {
		double summation = 0;
		if (!stations.isEmpty()) {
			double factor = 0.0;
			double multiplication = 1.0;
			double maxDistance = parameters.maxdistance;
			double distance = 0.0;
			for (Station s: stations) {
				distance = station.getPosition().distanceTo(s.getPosition());
				if (maxDistance > distance) {
					factor = (maxDistance - distance)/maxDistance;
				}
				multiplication = s.availableSlots()*factor;
				summation += multiplication; 
			}
		}
		return summation;
	}

	private Station nearestStationToRent(List<Station> stations, GeoPoint point) {
		Comparator<Station> byDistance = StationComparator.byDistance(point);
		List<Station> orderedStations = stations.stream().filter(s -> s.availableBikes() > 0)
				.sorted(byDistance).collect(Collectors.toList());
		return orderedStations.get(0);
	}
	
	private Station nearestStationToReturn(List<Station> stations, GeoPoint point) {
		Comparator<Station> byDistance = StationComparator.byDistance(point);
		List<Station> orderedStations = stations.stream().filter(s -> s.availableSlots() > 0)
				.sorted(byDistance).collect(Collectors.toList());
		return orderedStations.get(0);
	}


}
