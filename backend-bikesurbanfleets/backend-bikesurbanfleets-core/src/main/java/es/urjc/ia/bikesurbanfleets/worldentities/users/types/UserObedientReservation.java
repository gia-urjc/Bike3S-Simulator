/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.worldentities.users.types;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.recommendationSystemTypes.Recommendation;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.core.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserType;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author holger
 */
@UserType("USER_OBEDIENT_RES")
public class UserObedientReservation extends UserUninformedReservation {
    
        public UserObedientReservation(JsonObject userdef, SimulationServices services, long seed) throws Exception{
        super(userdef, services, seed);
      }

    @Override
    protected Station determineStationToRentBike() {
        
        Station destination = null;
        List<Recommendation> recommendedStations = recommendationSystem.getRecomendedStationToRentBike(this.getPosition()).stream()
                .filter(recomendation -> recomendation.getStation().getPosition().distanceTo(this.getPosition()) <= parameters.maxDistanceToRentBike).collect(Collectors.toList());
        if (!recommendedStations.isEmpty()) {
            destination = recommendedStations.get(0).getStation();
        }
        return destination;
    }

    @Override
    protected Station determineStationToReturnBike() {
        Station destination = null;
               
        List<Recommendation> recommendedStations = recommendationSystem.getRecomendedStationToReturnBike(destinationPlace);
        if (!recommendedStations.isEmpty()) {
        	destination = recommendedStations.get(0).getStation();
        } else {
            throw new RuntimeException("user cant return a bike, recomender did not tell return station");
        }
        return destination;
    }

}
