package es.urjc.ia.bikesurbanfleets.core.UserEvents;

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
    public EventUser execute() throws Exception {
        user.setPosition(position);
        debugEventLog("At enter the event");
        //set the result of the event
        //the result of EventUserAppears is always success
        setResultInfo(Event.RESULT_TYPE.SUCCESS, null);

        //decide what to do afterwards
        EventUser e = manageUserRentalDecision(DECISION_TYPE.AFTER_APPEARING);
        

        return e;
    }
}
