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
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecision;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecisionLeaveSystem;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecisionStation;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserType;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *
 * @author holger
 */
@UserType("USER_OBEDIENT")
public class UserObedient extends UserUninformed {

    private double lastexpetedProbability;
    private final boolean printHints = true;

    public UserObedient(JsonObject userdef, SimulationServices services, long seed) throws Exception {
        super(userdef, services, seed);
    }

    @Override
    public UserDecision decideAfterFailedRental() {
        if (printHints) {
            System.out.format("[Info] User: %3d Failed take. last probability was:  %9.8f %n" , this.getId(),lastexpetedProbability);
         }
//        if (getMemory().getRentalAttemptsCounter() >= parameters.minRentalAttempts) {
//            return new UserDecisionLeaveSystem();
//        } else {
        Station s = determineStationToRentBike();
        if (s != null) { //user has found a station
            double dist=s.getPosition().distanceTo(this.getPosition());
            if (dist<= parameters.maxDistanceToRentBike-getMemory().getWalkedToTakeBikeDistance()) {
                return new UserDecisionStation(s, false);
            }
        } //if not he would leave
        return new UserDecisionLeaveSystem();
    }

    @Override
    public UserDecisionStation decideAfterFailedReturn() {
        if (printHints) {
             System.out.format("[Info] User: %3d Failed return. last probability was:  %9.8f %n" , this.getId(),lastexpetedProbability);
        }
        Station s = determineStationToReturnBike();
        return new UserDecisionStation(s, false);
    }

    @Override
    protected Station determineStationToRentBike() {

        Station destination = null;
        if (printHints) {
             System.out.println("User: "+ this.getId() + " asks for renting recommendation%n");
        }
        List<Recommendation> originRecommendedStations = recommendationSystem.getRecomendedStationsToRentBike(this.getPosition());
        List<Recommendation> recommendedStations = originRecommendedStations.stream()
                .filter(recomendation -> recomendation.getStation().getPosition().distanceTo(this.getPosition()) <= parameters.maxDistanceToRentBike).collect(Collectors.toList());
        boolean noStationAtdist=recommendedStations.isEmpty();

        List<Station> triedStations = getMemory().getStationsWithRentalFailedAttempts();
        removeTriedStations(recommendedStations, triedStations);

        if (!recommendedStations.isEmpty()) {
            destination = recommendedStations.get(0).getStation();
            lastexpetedProbability = recommendedStations.get(0).getProbability();
        } else {
            if (printHints) {
                if (!originRecommendedStations.isEmpty() && noStationAtdist) {
                    System.out.println("[Warn] User " + this.getId() + " not accepted recommended station when taking because of distance: " + originRecommendedStations.get(0).getStation().getPosition().distanceTo(this.getPosition()));
                } else if (!originRecommendedStations.isEmpty() && !noStationAtdist) {
                    System.out.println("[Warn] User " + this.getId() + " no station used (all recommendation tried already) ");
                } else {
                    System.out.println("[Warn] User " + this.getId() + " no recommendation obtained ");
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
        List<Recommendation> recommendedStations = recommendationSystem.getRecomendedStationsToReturnBike(this.getPosition(), destinationPlace);
        List<Station> triedStations = getMemory().getStationsWithReturnFailedAttempts();
        removeTriedStations(recommendedStations, triedStations);
        if (!recommendedStations.isEmpty()) {
            destination = recommendedStations.get(0).getStation();
            lastexpetedProbability = recommendedStations.get(0).getProbability();
        } else {   
            System.out.println("[Warn] User " + this.getId() + " no (new) return station recommended, will try return at closest station with slot");
            List<Station> finalStations = informationSystem.getStationsWithAvailableSlotsOrderedByDistance(this.destinationPlace);
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
