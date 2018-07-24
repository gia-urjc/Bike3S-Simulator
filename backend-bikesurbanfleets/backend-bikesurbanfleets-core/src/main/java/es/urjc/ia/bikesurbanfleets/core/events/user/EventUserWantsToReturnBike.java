package es.urjc.ia.bikesurbanfleets.core.events.user;

import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.common.util.MessageGuiFormatter;
import es.urjc.ia.bikesurbanfleets.users.User;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventUserWantsToReturnBike extends EventUser {

    private List<Entity> entities;
    private GeoPoint currentPosition;

    public EventUserWantsToReturnBike(int instant, User user, GeoPoint actualPosition) {
        super(instant, user);
        this.entities = new ArrayList<>(Arrays.asList(user));
        this.currentPosition = actualPosition;
    }

    public GeoPoint getActualPosition() {
        return currentPosition;
    }

    @Override
    public List<Event> execute() {
        List<Event> newEvents = new ArrayList<>();
        try {
            user.setInstant(getInstant());
            user.setPosition(currentPosition);
            debugEventLog();
            newEvents = manageSlotReservationDecisionAtOtherStation();
        }
        catch(Exception e) {
            exceptionTreatment(e);
        }
        return newEvents;

    }

    @Override
    public List<Entity> getEntities() {
        return entities;
    }
}
