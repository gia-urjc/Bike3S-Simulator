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
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecisionGoToStation;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecisionLeaveSystem;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecisionReserveBike;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecisionReserveSlot;
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

    public UserObedient(JsonObject userdef, SimulationServices services, long seed) throws Exception {
        super(userdef, services, seed);
        printHints = false;
    }

    @Override
    public UserDecision decideAfterFailedRental() {
        if (printHints) {
                UserInformed.ratio=(UserInformed.ratio * UserInformed.rationumber)
                        + (getMemory().getWalkedToTakeBikeDistance() - lastwalked)/lastdist;
                UserInformed.rationumber++;
                UserInformed.ratio=UserInformed.ratio/UserInformed.rationumber;               
                System.out.format("[UserInfo] Total ratio real/estimated distance: %f. %n", UserInformed.ratio);
            System.out.format("[UserInfo] User: %d after failed rental. Estimated distance %f. Real distance %f. Ratio %f.%n", this.getId(),
                    lastdist, getMemory().getWalkedToTakeBikeDistance() - lastwalked,   (getMemory().getWalkedToTakeBikeDistance() - lastwalked)/lastdist);
        }
        if (printHints) {
            System.out.format("[UserInfo] User: %d Failed take. last probability was:  %9.8f; has walked %f meters of %f maximum.%n",
                    this.getId(), lastexpetedProbability, getMemory().getWalkedToTakeBikeDistance(), parameters.maxDistanceToRentBike);
        }
//        if (getMemory().getRentalAttemptsCounter() >= parameters.minRentalAttempts) {
//            return new UserDecisionLeaveSystem();
//        } else {
        Station s = determineStationToRentBike();
        if (s != null) { //user has found a station
            double dist=routeService.estimateDistance(this.getPosition(), s.getPosition(), "foot");
            if (dist <= parameters.maxDistanceToRentBike - getMemory().getWalkedToTakeBikeDistance()) {
                lastdist = dist;
                lastwalked = getMemory().getWalkedToTakeBikeDistance();
                return new UserDecisionGoToStation(s);
            }
            if (printHints) {
                System.out.format("[UserInfo] User: %d abandons after failed rental. Station at distance %f found. But has walked %f meters of %f maximum.%n", this.getId(),
                        dist, getMemory().getWalkedToTakeBikeDistance(), parameters.maxDistanceToRentBike);
            }
            return new UserDecisionLeaveSystem();
        } //if not he would leave
        if (printHints) {
            System.out.format("[UserInfo] User: %d abandons after failed rental. No station found. Has walked %f meters of %f maximum.%n", this.getId(),
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

        Station destination = null;
        if (printHints) {
            System.out.format("[UserInfo] User: %d asks for renting recommendation; has walked %f meters of %f maximum.%n",
                    this.getId(), getMemory().getWalkedToTakeBikeDistance(), parameters.maxDistanceToRentBike);
        }
        double desiredmaxdistance = Math.max(0, parameters.maxDistanceToRentBike - getMemory().getWalkedToTakeBikeDistance());
        List<Recommendation> originRecommendedStations = recommendationSystem.getRecomendedStationsToRentBike(this.getPosition(), desiredmaxdistance);
        List<Recommendation> recommendedStations = originRecommendedStations.stream()
                .filter(recomendation -> 
                        routeService.estimateDistance(this.getPosition(), recomendation.getStation().getPosition(), "foot")
                         <= desiredmaxdistance).collect(Collectors.toList());
        boolean noStationAtdist = recommendedStations.isEmpty();

        List<Station> triedStations = getMemory().getStationsWithRentalFailedAttempts();
        removeTriedStations(recommendedStations, triedStations);

        if (!recommendedStations.isEmpty()) {
            destination = recommendedStations.get(0).getStation();
            lastexpetedProbability = recommendedStations.get(0).getProbability();
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
            System.out.format("[UserInfo] User: %d asks for return recommendation; has walked %f meters of %f maximum.%n",
                    this.getId(), getMemory().getWalkedToTakeBikeDistance(), parameters.maxDistanceToRentBike);
        }
        List<Recommendation> recommendedStations = recommendationSystem.getRecomendedStationsToReturnBike(this.getPosition(), destinationPlace);
        List<Station> triedStations = getMemory().getStationsWithReturnFailedAttempts();
        removeTriedStations(recommendedStations, triedStations);
        if (!recommendedStations.isEmpty()) {
            destination = recommendedStations.get(0).getStation();
            lastexpetedProbability = recommendedStations.get(0).getProbability();
        } else {
            System.out.println("[UserInfo] User " + this.getId() + " no (new) return station recommended, will try return at closest station with slot");
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
