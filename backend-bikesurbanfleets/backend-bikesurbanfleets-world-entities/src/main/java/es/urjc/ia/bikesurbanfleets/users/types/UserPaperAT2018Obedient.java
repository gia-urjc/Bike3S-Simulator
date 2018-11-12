/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.users.types;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.consultSystems.recommendationSystemTypes.Recommendation;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.users.UserType;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author holger
 */
@UserType("USER_PAPERAT2018_OBHOLGER")
public class UserPaperAT2018Obedient extends UserPaperAT2018Uninformed {
    
        public UserPaperAT2018Obedient(JsonObject userdef, SimulationServices services, long seed) throws Exception{
        super(userdef, services, seed);
      }

    @Override
    public Station determineStationToRentBike() {
        
        Station destination = null;
        List<Recommendation> recommendedStations = recommendationSystem.recommendStationToRentBike(this.getPosition()).stream()
                .filter(recomendation -> recomendation.getStation().getPosition().distanceTo(this.getPosition()) <= parameters.maxDistanceToRentBike).collect(Collectors.toList());
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
        } else {
            throw new RuntimeException("user cant return a bike, recomender did not tell return station");
        }
        return destination;
    }

}
