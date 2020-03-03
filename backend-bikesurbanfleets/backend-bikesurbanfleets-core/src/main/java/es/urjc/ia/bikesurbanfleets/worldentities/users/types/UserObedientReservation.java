/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.worldentities.users.types;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.Recommendation;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserType;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *
 * @author holger
 */
@UserType("USER_OBEDIENT_RES")
public class UserObedientReservation extends UserUninformedReservation {
    
    private final boolean printHints = false;

    public UserObedientReservation(JsonObject userdef, SimulationServices services, long seed) throws Exception{
        super(userdef, services, seed);
    }

    @Override
    protected Station determineStationToRentBike() {
        Station destination = null;
        if (printHints) {
             System.out.println("User: "+ this.getId() + " asks for renting recommendation%n");
        }
        double desiredmaxdistance=Math.max(0,parameters.maxDistanceToRentBike-getMemory().getWalkedToTakeBikeDistance());
        List<Recommendation> originRecommendedStations = recommendationSystem.getRecomendedStationsToRentBike(this.getPosition(),desiredmaxdistance);
        List<Recommendation> recommendedStations = originRecommendedStations.stream()
                .filter(recomendation -> 
                        routeService.estimateDistance(this.getPosition(), recomendation.getStation().getPosition(), "foot")
                         <= desiredmaxdistance).collect(Collectors.toList());
        boolean noStationAtdist=recommendedStations.isEmpty();

        List<Station> triedStations = getMemory().getStationsWithReservationRentalFailedAttempts();
        removeTriedStations(recommendedStations, triedStations);

        if (!recommendedStations.isEmpty()) {
            destination = recommendedStations.get(0).getStation();
        } else {
            if (printHints) {
                if (!originRecommendedStations.isEmpty() && noStationAtdist) {
                    System.out.println("[UserInfo] User " + this.getId() + " not accepted recommended station when taking because of distance: " 
                            + routeService.estimateDistance(this.getPosition(), originRecommendedStations.get(0).getStation().getPosition(), "foot"));
                } else if (!originRecommendedStations.isEmpty() && !noStationAtdist) {
                    System.out.println("[UserInfo] User " + this.getId() + " no station used (all recommendation tried already) ");
                } else {
                    System.out.println("[UserInfo] User " + this.getId() + " no recommendation obtained ");
                 }
            }
        }
        return destination;
    }
    private void removeTriedStations(List<Recommendation> rec, List<Station> tried) {

        Predicate<Recommendation> pr;
        if (tried.size() > 0) {
            for (Station stried : tried) {
                pr = p -> p.getStation() == stried;
                rec.removeIf(pr);
            }
        }
    }

    @Override
    protected Station determineStationToReturnBike() {
        Station destination = null;
        if (printHints) {
             System.out.println("User: "+ this.getId() + " asks for return recommendation%n");
        }
        List<Recommendation> recommendedStations = recommendationSystem.getRecomendedStationsToReturnBike( this.getPosition(), destinationPlace);
        List<Station> triedStations = getMemory().getStationsWithReservationReturnFailedAttempts();
        removeTriedStations(recommendedStations, triedStations);

        if (!recommendedStations.isEmpty()) {
            destination = recommendedStations.get(0).getStation();
        } else {            
            System.out.println("[Warn] User " + this.getId() + " no (new) return station recommended, will try return at closest station with slot");
            List<Station> finalStations = informationSystem.getStationsWithAvailableSlotsOrderedByDistance(this.destinationPlace,"foot");
            if (!finalStations.isEmpty()) {
                destination = finalStations.get(0);
            } else {
                throw new RuntimeException("[Error] User " + this.getId() + " cant return a bike, no slots");
            }
            return destination;
        }
        return destination;
    }

}
