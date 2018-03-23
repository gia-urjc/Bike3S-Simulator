package es.urjc.ia.bikesurbanfleets.core.events;

import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Entity;
import es.urjc.ia.bikesurbanfleets.entities.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventUserAppears extends EventUser {

    private List<Entity> entities;
    private GeoPoint position;
    
    public EventUserAppears(int instant, User user, GeoPoint position) {
        super(instant, user);
        this.entities = new ArrayList<>(Arrays.asList(user));
        this.position = position;
    }

    @Override
    public List<Event> execute() throws Exception {
        user.setPosition(position);
        debugEventLog();
        return manageBikeReservationDecisionAtOtherStation();
    }

    @Override
    public List<Entity> getEntities() {
        return entities;
    }
}