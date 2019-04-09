/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.worldentities.users.types;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.Recommendation;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.core.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecision;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecisionGoToPointInCity;
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
    private final boolean printHints = false;

    public UserObedient(JsonObject userdef, SimulationServices services, long seed) throws Exception {
        super(userdef, services, seed);
    }

    @Override
    public UserDecision decideAfterFailedRental() {
        if (printHints) {
            System.out.format("Failed take. last probability was:  %9.8f %n" , lastexpetedProbability);
         }
        if (getMemory().getRentalAttemptsCounter() >= parameters.minRentalAttempts) {
            return new UserDecisionLeaveSystem();
        } else {
            Station s = determineStationToRentBike();
            if (s != null) { //user has found a station
                return new UserDecisionStation(s, false);
            } else {
                return new UserDecisionLeaveSystem();
            }
        }
    }

    @Override
    public UserDecisionStation decideAfterFailedReturn() {
        if (printHints) {
             System.out.format("Failed return. last probability was:  %9.8f %n" , lastexpetedProbability);
        }
        Station s = determineStationToReturnBike();
        return new UserDecisionStation(s, false);
    }

    @Override
    protected Station determineStationToRentBike() {

        Station destination = null;
        List<Recommendation> originRecommendedStations = recommendationSystem.getRecomendedStationToRentBike(this.getPosition());
        List<Recommendation> recommendedStations = originRecommendedStations.stream()
                .filter(recomendation -> recomendation.getStation().getPosition().distanceTo(this.getPosition()) <= parameters.maxDistanceToRentBike).collect(Collectors.toList());

        List<Station> triedStations = getMemory().getStationsWithRentalFailedAttempts();
        removeTriedStations(recommendedStations, triedStations);

        if (!recommendedStations.isEmpty()) {
            destination = recommendedStations.get(0).getStation();
            lastexpetedProbability = recommendedStations.get(0).getProbability();
        } else {
            if (printHints) {
                if (!originRecommendedStations.isEmpty()) {
                    System.out.println("not accepted recommended station when taking because of distance: " + originRecommendedStations.get(0).getStation().getPosition().distanceTo(this.getPosition()));
                } else {
                    System.out.println("not recommended station whent taking ");
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

        List<Recommendation> recommendedStations = recommendationSystem.getRecomendedStationToReturnBike(this.getPosition(), destinationPlace);
        List<Station> triedStations = getMemory().getStationsWithReturnFailedAttempts();
        removeTriedStations(recommendedStations, triedStations);
        if (!recommendedStations.isEmpty()) {
            destination = recommendedStations.get(0).getStation();
            lastexpetedProbability = recommendedStations.get(0).getProbability();
        } else {
            throw new RuntimeException("user cant return a bike, recomender did not tell return station");
        }
        return destination;
    }

}
