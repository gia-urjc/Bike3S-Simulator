package es.urjc.ia.bikesurbanfleets.core.events;

import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.worldentities.users.User;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecision;

import java.util.ArrayList;
import java.util.Arrays;

public class EventUserAppears extends EventUser {

    private GeoPoint position;

    public EventUserAppears(int instant, User user, GeoPoint position) {
        super(instant, user);
        this.involvedEntities = new ArrayList<>(Arrays.asList(user));
        this.newEntities = new ArrayList<>(Arrays.asList(user));
        this.oldEntities=null;
        this.position = position;
        
    }

    @Override
    public Event execute() throws Exception {
        user.setPosition(position);
        debugEventLog("At enter the event");
        UserDecision ud = user.decideAfterAppearning();
        Event e = manageUserRentalDecision(ud, Event.EXIT_REASON.EXIT_AFTER_APPEARING);
        
        //set the result of the event
        //the result of EventUserAppears is always success
        setResult(Event.RESULT_TYPE.SUCCESS);

        return e;
    }
}
