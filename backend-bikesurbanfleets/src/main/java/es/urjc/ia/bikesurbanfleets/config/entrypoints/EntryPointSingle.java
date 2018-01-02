package es.urjc.ia.bikesurbanfleets.config.entrypoints;

import es.urjc.ia.bikesurbanfleets.entities.users.User;
import es.urjc.ia.bikesurbanfleets.entities.users.UserFactory;
import es.urjc.ia.bikesurbanfleets.events.EventUserAppears;
import es.urjc.ia.bikesurbanfleets.graphs.GeoPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a unic user who appears at a specific position.
 * @author IAgroup
 *
 */
public class EntryPointSingle extends EntryPoint {
    /**
     * It is the point where user appears, i. e., where user is located after being generated.
     */
    private GeoPoint position;
    
    /**
     * Type of user that will be generated.
     */
    private EntryPointUserProperties userType;
    
    /**
     * It is the time instant when user appears at the system.
     */
    private int timeInstant; 
    
    public EntryPointSingle(GeoPoint position, EntryPointUserProperties userType, int instant) {
        this.position = position;
        this.userType = userType;
        this.timeInstant = instant;
    }

    @Override
    public List<EventUserAppears> generateEvents() {
        List<EventUserAppears> generatedEvents = new ArrayList<>();
        UserFactory userFactory = new UserFactory();
        User user = userFactory.createUser(userType);
        EventUserAppears event = new EventUserAppears(timeInstant, user, position);
        generatedEvents.add(event);
        return generatedEvents;
    }
    
    public String toString() {
        String result = position.toString();
        result += "| SINGLE user \n";
        result += "user Type: " + userType + "\n";
        result += "Instant: " + timeInstant + "\n";
        return result;
    }
}
