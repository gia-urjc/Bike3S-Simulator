package es.urjc.ia.bikesurbanfleets.core.events;

import java.util.ArrayList;
import java.util.Arrays;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.users.User;

public class EventUserLeavesSystem extends EventUser {

    Event.EXIT_REASON reason;
    
    public EventUserLeavesSystem(int instant, User user, Event.EXIT_REASON reason) {
        super(instant, user);
        this.involvedEntities = new ArrayList<>(Arrays.asList(user));
        this.newEntities = null;
        this.oldEntities=new ArrayList<>(Arrays.asList(user));
        this.reason=reason;
    }

    @Override
    public Event execute() throws Exception {
        debugEventLog("At enter the event");
        user.leaveSystem();
        user.setState(User.STATE.LEFT_SYSTEM);
        debugEventLog("User left the system");
        debugClose(user, user.getId());
       
        //set the result of the event
        //the result of EventUserLeavesSystem is any of the possible exit rerasons
        setResult(this.reason);

        return null;
    }
}
