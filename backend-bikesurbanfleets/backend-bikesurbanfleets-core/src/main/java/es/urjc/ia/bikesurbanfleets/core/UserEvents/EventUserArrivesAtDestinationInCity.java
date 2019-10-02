package es.urjc.ia.bikesurbanfleets.core.UserEvents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.worldentities.users.User;

public class EventUserArrivesAtDestinationInCity extends EventUser {

    private GeoPoint position;

    public EventUserArrivesAtDestinationInCity(int instant, User user, GeoPoint position) {
        super(instant, user);
        this.involvedEntities = new ArrayList<>(Arrays.asList(user));
        this.newEntities = null;
        this.oldEntities=null;
        this.position = position;
    }

    @Override
    public EventUser execute() throws Exception {
        user.setPosition(position);
        debugEventLog("At enter the event");
        user.setState(User.STATE.LEAVING);
        debugEventLog("User leaves the system");
        //set the result of the event
        //the result of EventUserArrivesAtDestinationInCity is always success
        setResultInfo(Event.RESULT_TYPE.SUCCESS, null);

        //decide what to do afterwards
        EventUser e = new EventUserLeavesSystem(this.getInstant(), user, EventUser.EXIT_REASON.EXIT_AFTER_REACHING_DESTINATION);

        return e;
     }
}
