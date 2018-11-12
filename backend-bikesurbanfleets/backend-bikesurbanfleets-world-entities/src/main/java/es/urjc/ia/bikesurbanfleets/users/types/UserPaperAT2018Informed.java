/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.users.types;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.users.UserType;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author holger
 */
@UserType("USER_PAPERAT2018_INF")
public class UserPaperAT2018Informed extends UserPaperAT2018Uninformed {

    public UserPaperAT2018Informed(JsonObject userdef, SimulationServices services, long seed) throws Exception {
        super(userdef, services, seed);
    }

    @Override
    public Station determineStationToRentBike() {

        Station destination = null;
        List<Station> finalStations = informationSystem.getStationsToRentBikeOrderedByDistance(this.getPosition()).stream()
                .filter(station -> station.getPosition().distanceTo(this.getPosition()) <= parameters.maxDistanceToRentBike).collect(Collectors.toList());
        if (!finalStations.isEmpty()) {
            destination = finalStations.get(0);
        }
        return destination;
    }

    @Override
    public Station determineStationToReturnBike() {
        Station destination = null;
        List<Station> recommendedStations = informationSystem.getStationsToReturnBikeOrderedByDistance(this.destinationPlace);
        if (!recommendedStations.isEmpty()) {
            destination = recommendedStations.get(0);
        } else {
            throw new RuntimeException("user cant return a bike, no slots");
        }
        return destination;
    }
}
