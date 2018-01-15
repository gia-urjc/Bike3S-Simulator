package es.urjc.ia.bikesurbanfleets.usersgenerator.config.entrypoints;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.usersgenerator.config.SingleUser;
import es.urjc.ia.bikesurbanfleets.usersgenerator.config.UserProperties;
import es.urjc.ia.bikesurbanfleets.usersgenerator.config.entrypoints.EntryPoint;

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
    private UserProperties userType;
    
    /**
     * It is the time instant when user appears at the system.
     */
    private int timeInstant; 
    
    public EntryPointSingle(GeoPoint position, UserProperties userType, int instant) {
        this.position = position;
        this.userType = userType;
        this.timeInstant = instant;
    }

    @Override
    public List<SingleUser> generateUsers() {
        List<SingleUser> singleUsers = new ArrayList<>();
        SingleUser user = new SingleUser(position, userType, timeInstant);
        singleUsers.add(user);
        return singleUsers;
    }
    
    public String toString() {
        String result = position.toString();
        result += "| SINGLE user \n";
        result += "user Type: " + userType + "\n";
        result += "Instant: " + timeInstant + "\n";
        return result;
    }
}
