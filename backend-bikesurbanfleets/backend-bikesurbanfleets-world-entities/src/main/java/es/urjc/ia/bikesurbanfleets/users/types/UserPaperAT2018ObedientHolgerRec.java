/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.users.types;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.consultSystems.recommendationSystems.Recommendation;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.users.UserType;
import java.util.List;

/**
 *
 * @author holger
 */
@UserType("USER_PAPERAT2018_OBHOLGER")
public class UserPaperAT2018ObedientHolgerRec extends UserPaperAT2018Uninformed {
    
        public UserPaperAT2018ObedientHolgerRec(JsonObject userdef, SimulationServices services, long seed) throws Exception{
        super(userdef, services, seed);
      }

    @Override
    public Station determineStationToRentBike() {
        Station destination = null;
        List<Recommendation> recommendedStations = recommendationSystem.recommendStationToRentBike(this.getPosition());
        //Remove station if the user is in this station
 //       recommendedStations.removeIf(recommendation -> recommendation.getStation().getPosition().equals(this.getPosition()) && recommendation.getStation().availableBikes() == 0);
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

}
