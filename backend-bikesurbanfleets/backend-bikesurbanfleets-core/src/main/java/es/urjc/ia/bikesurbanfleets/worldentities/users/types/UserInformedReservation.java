/*

 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.worldentities.users.types;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
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
        Station destination = null;
        List<Station> triedStations = getMemory().getStationsWithReservationRentalFailedAttempts(); 
        List<Station> finalStations = informationSystem.getStationsWithAvailableBikesOrderedByDistance(this.getPosition()).stream()
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
        List<Station> finalStations = informationSystem.getStationsWithAvailableSlotsOrderedByDistance(this.destinationPlace);
        finalStations.removeAll(triedStations);
        if (!finalStations.isEmpty()) {
            destination = finalStations.get(0);
        } else {
            throw new RuntimeException("[Error] User " + this.getId() + " cant return a bike, no slots");
        }
        return destination;
    }

}
