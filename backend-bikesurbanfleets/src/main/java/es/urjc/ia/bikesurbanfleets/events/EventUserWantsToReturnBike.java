package es.urjc.ia.bikesurbanfleets.events;

import es.urjc.ia.bikesurbanfleets.entities.Entity;
import es.urjc.ia.bikesurbanfleets.entities.users.User;
import es.urjc.ia.bikesurbanfleets.graphs.GeoPoint;

import java.util.Arrays;
import java.util.List;

public class EventUserWantsToReturnBike extends EventUser {

    private List<Entity> entities;
    private GeoPoint actualPosition;

    public EventUserWantsToReturnBike(int instant, User user, GeoPoint actualPosition) {
        super(instant, user);
        this.entities = Arrays.asList(user);
        this.actualPosition = actualPosition;
    }

    public GeoPoint getActualPosition() {
        return actualPosition;
    }

    @Override
    public List<Event> execute() throws Exception {
        user.setPosition(actualPosition);
        return manageSlotReservationDecisionAtOtherStation();
    }

    @Override
    public List<Entity> getEntities() {
        return entities;
    }
}
