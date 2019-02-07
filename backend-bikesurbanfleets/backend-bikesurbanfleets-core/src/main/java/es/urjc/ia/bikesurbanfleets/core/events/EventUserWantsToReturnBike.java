package es.urjc.ia.bikesurbanfleets.core.events;

import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.worldentities.users.User;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Entity;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecision;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecisionGoToPointInCity;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecisionStation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventUserWantsToReturnBike extends EventUser {

    private GeoPoint currentPosition;

    public EventUserWantsToReturnBike(int instant, User user, GeoPoint actualPosition) {
        super(instant, user);
        this.involvedEntities = new ArrayList<>(Arrays.asList(user));
        this.newEntities = null;
        this.oldEntities = null;
        this.currentPosition = actualPosition;
    }

    @Override
    public Event execute() throws Exception {
        user.setInstant(this.instant);
        user.setPosition(currentPosition);
        debugEventLog("At enter the event");
        UserDecisionStation ud = user.decideAfterFinishingRide();
        Event e = manageUserReturnDecision(ud);
        
        //set the result of the event
        //the result of EventUserWantsToReturnBike is always success
        setResult(Event.RESULT_TYPE.SUCCESS);

        return e;
    }
}
