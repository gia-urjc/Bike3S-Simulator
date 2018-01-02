package es.urjc.ia.bikesurbanfleets.events;

import es.urjc.ia.bikesurbanfleets.entities.Entity;
import es.urjc.ia.bikesurbanfleets.entities.users.User;
import es.urjc.ia.bikesurbanfleets.graphs.GeoPoint;

import java.util.Arrays;
import java.util.List;

public class EventUserAppears extends EventUser {

    private List<Entity> entities;
    private GeoPoint position;
    
    public EventUserAppears(int instant, User user, GeoPoint position) {
        super(instant, user);
        this.entities = Arrays.asList(user);
        this.position = position;
    }

    @Override
    public List<Event> execute() throws Exception {
        user.setPosition(position);
        return manageBikeReservationDecisionAtOtherStation();
    }

    @Override
    public List<Entity> getEntities() {
        return entities;
    }
}