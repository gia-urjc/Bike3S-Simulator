package es.urjc.ia.bikesurbanfleets.core.events;

import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.common.util.MessageGuiFormatter;
import es.urjc.ia.bikesurbanfleets.users.User;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Entity;

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
    public List<Event> execute() {
        List<Event> newEvents = new ArrayList<>();
        try {
            user.setInstant(this.instant);
            user.setPosition(position);
            debugEventLog();
            newEvents = manageBikeReservationDecisionAtOtherStation();
        }
        catch(Exception e) {
            MessageGuiFormatter.showErrorsForGui(e);
            user.setPosition(null);
            user.setRoute(null);
            user.setDestinationPoint(null);
            user.setDestinationStation(null);
        }

        return newEvents;
    }

    @Override
    public List<Entity> getEntities() {
        return entities;
    }
}