/*

 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.worldentities.users.types;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.services.Recommendation;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserType;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author alberto
 */
@UserType("USER_INFORMED_RES")
public class UserInformedReservation extends UserUninformedReservation {
    
        public UserInformedReservation(JsonObject userdef, SimulationServices services, long seed) throws Exception{
        super(userdef, services, seed);
      }
    @Override
    protected Station determineStationToRentBike() {
        double desiredmaxdistance = Math.max(0, parameters.maxDistanceToRentBike - getMemory().getWalkedToTakeBikeDistance());

        List<Recommendation> stations = informationSystem.getStationsOrderedByWalkDistanceWithinMaxDistanceWithBikes(this.getPosition(), desiredmaxdistance);
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
        List<Recommendation> stations = informationSystem.getAllStationsOrderedByWalkDistanceWithSlots(destinationPlace);
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
