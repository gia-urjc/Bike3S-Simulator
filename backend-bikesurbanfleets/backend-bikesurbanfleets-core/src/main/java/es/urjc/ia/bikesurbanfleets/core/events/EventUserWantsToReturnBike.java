package es.urjc.ia.bikesurbanfleets.core.events;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.core.entities.Entity;
import es.urjc.ia.bikesurbanfleets.core.entities.users.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventUserWantsToReturnBike extends EventUser {

    private List<Entity> entities;
    private GeoPoint actualPosition;

    public EventUserWantsToReturnBike(int instant, User user, GeoPoint actualPosition) {
        super(instant, user);
        this.entities = new ArrayList<>(Arrays.asList(user));
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
