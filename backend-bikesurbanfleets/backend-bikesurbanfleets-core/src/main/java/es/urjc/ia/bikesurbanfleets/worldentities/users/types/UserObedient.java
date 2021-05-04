/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.worldentities.users.types;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.services.Recommendation;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecision;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecisionGoToStation;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecisionLeaveSystem;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserType;
import java.util.List;
import java.util.function.Predicate;

/**
 *
 * @author holger
 */
@UserType("USER_OBEDIENT")
public class UserObedient extends UserUninformed {

    private double lastexpetedProbability;

    public UserObedient(JsonObject userdef, SimulationServices services, long seed) throws Exception {
        super(userdef, services, seed);
    }

    @Override
    public UserDecision decideAfterFailedRental() {
        if (printHints) {
            System.out.format("[UserInfo] User: %d Failed take. last probability was:  %9.8f; has walked %f meters of %f maximum.%n",
                    this.getId(), lastexpetedProbability, getMemory().getWalkedToTakeBikeDistance(), parameters.maxDistanceToRentBike);
        }
        Station s = determineStationToRentBike();
        if (s != null) { //user has found a station
            return new UserDecisionGoToStation(s);
        } //if not he would leave
        if (printHints) {
            System.out.format("[UserInfo] User: %d abandons after failed rental. No station found within acceptable distance.. Has walked already %f meters of %f maximum.%n", this.getId(),
                    getMemory().getWalkedToTakeBikeDistance(), parameters.maxDistanceToRentBike);
        }
        return new UserDecisionLeaveSystem();
    }

    @Override
    public UserDecision decideAfterFailedReturn() {
        if (printHints) {
            System.out.format("[UserInfo] User: %d Failed return. last probability was:  %9.8f %n", this.getId(), lastexpetedProbability);
        }
        Station s = determineStationToReturnBike();
        return new UserDecisionGoToStation(s);
    }

    @Override
    protected Station determineStationToRentBike() {
        if (printHints) {
            System.out.format("[UserInfo] User: %d asks for renting recommendation; has walked %f meters of %f maximum.%n",
                    this.getId(), getMemory().getWalkedToTakeBikeDistance(), parameters.maxDistanceToRentBike);
        }
        double desiredmaxdistance = Math.max(0, parameters.maxDistanceToRentBike - getMemory().getWalkedToTakeBikeDistance());
        List<Recommendation> recommendedStations = recommendationSystem.getRecomendedStationsToRentBike(this.getPosition(), desiredmaxdistance);
        boolean noStationsRecommended = recommendedStations.isEmpty();

        List<Station> triedStations = getMemory().getStationsWithRentalFailedAttempts();
        removeTriedStations(recommendedStations, triedStations);

        if (!recommendedStations.isEmpty()) {
            lastexpetedProbability = recommendedStations.get(0).getProbability();
            return recommendedStations.get(0).getStation();
        } else {
            if (printHints) {
                if (noStationsRecommended) {
                    System.out.format("[UserInfo] User " + this.getId() + " no recommendation obtained within the maximum distance of %f meters.%n", desiredmaxdistance);
                } else {
                    System.out.println("[UserInfo] User " + this.getId() + " no station used (all recommendations tried already)");
                }
            }
        }
        return null;
    }

    @Override
    protected Station determineStationToReturnBike() {
        if (printHints) {
            System.out.format("[UserInfo] User: %d asks for return recommendation; has cycled %f meters.%n",
                    this.getId(), getMemory().getDistanceTraveledByBike());
        }
        List<Recommendation> recommendedStations = recommendationSystem.getRecomendedStationsToReturnBike(this.getPosition(), destinationPlace);
        boolean noStationsRecommended = recommendedStations.isEmpty();

        List<Station> triedStations = getMemory().getStationsWithReturnFailedAttempts();
        removeTriedStations(recommendedStations, triedStations);

        if (!recommendedStations.isEmpty()) {
            lastexpetedProbability = recommendedStations.get(0).getProbability();
            return recommendedStations.get(0).getStation();
        } else {
            throw new RuntimeException("[Error] User " + this.getId() + " cant return a bike, no station recommended or all are tried");
        }
    }

}
