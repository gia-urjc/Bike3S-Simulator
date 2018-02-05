package es.urjc.ia.bikesurbanfleets.users.types;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoRoute;
import es.urjc.ia.bikesurbanfleets.common.graphs.exceptions.GeoRouteException;
import es.urjc.ia.bikesurbanfleets.common.util.SimulationRandom;
import es.urjc.ia.bikesurbanfleets.entities.Station;
import es.urjc.ia.bikesurbanfleets.entities.User;
import es.urjc.ia.bikesurbanfleets.users.AssociatedType;
import es.urjc.ia.bikesurbanfleets.users.UserType;

import java.util.ArrayList;
import java.util.List;

@AssociatedType(UserType.USER_UNIFORMED)
public class UserUniform extends User{

    public UserUniform() {
        super();
    }

    @Override
    public boolean decidesToLeaveSystemAfterTimeout(int instant) {
        return systemManager.getRandom().nextBoolean();
    }


    @Override
    public boolean decidesToLeaveSystemAffterFailedReservation(int instant) {
        return systemManager.getRandom().nextBoolean();
    }


    @Override
    public boolean decidesToLeaveSystemWhenBikesUnavailable(int instant) {
        return systemManager.getRandom().nextBoolean();
    }

    @Override
    public Station determineStationToRentBike(int instant) {
        List<Station> stations = systemManager.consultStationsWithoutBikeReservationAttempt(this, instant);
        Station destination = null;

        if (!stations.isEmpty()) {
            List<Station> recommendedStations = systemManager.getRecommendationSystem()
                    .recommendByLinearDistance(this.getPosition(), stations);

            destination = recommendedStations.get(0).getPosition().equals(this.getPosition()) && recommendedStations.size() > 1
                    ? recommendedStations.get(1) : recommendedStations.get(0);
        }

        return destination;

    }

    @Override
    public Station determineStationToReturnBike(int instant) {
        List<Station> stations = systemManager.consultStationsWithoutSlotReservationAttempt(this, instant);

        if (stations.isEmpty()) {
            stations = new ArrayList<>(systemManager.consultStations());
        }

        List<Station> recommendedStations = systemManager.getRecommendationSystem()
                .recommendByLinearDistance(this.getPosition(), stations);
        // TODO: what happens if recommended stations size is 1?
        return recommendedStations.get(0).getPosition().equals(this.getPosition())
                ? recommendedStations.get(1) : recommendedStations.get(0);
    }

    @Override
    public boolean decidesToReserveBikeAtSameStationAfterTimeout() {
        return systemManager.getRandom().nextBoolean();
    }

    @Override
    public boolean decidesToReserveBikeAtNewDecidedStation() {
        return systemManager.getRandom().nextBoolean();
    }

    @Override
    public boolean decidesToReserveSlotAtSameStationAfterTimeout() {
        return systemManager.getRandom().nextBoolean();
    }

    @Override
    public boolean decidesToReserveSlotAtNewDecidedStation() {
        return systemManager.getRandom().nextBoolean();
    }

    @Override
    public GeoPoint decidesNextPoint() {
        return systemManager.generateBoundingBoxRandomPoint(SimulationRandom.getGeneralInstance());
    }

    @Override
    public boolean decidesToReturnBike() {
        return systemManager.getRandom().nextBoolean();
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterTimeout() {
        return systemManager.getRandom().nextBoolean();
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterFailedReservation() {
        return systemManager.getRandom().nextBoolean();
    }

    @Override
    public GeoRoute determineRoute(List<GeoRoute> routes) throws GeoRouteException {
        if (routes.isEmpty()) {
            throw new GeoRouteException("Route is not valid");
        }
        int index = systemManager.getRandom().nextInt(0, routes.size());
        return routes.get(index);
    }
}
