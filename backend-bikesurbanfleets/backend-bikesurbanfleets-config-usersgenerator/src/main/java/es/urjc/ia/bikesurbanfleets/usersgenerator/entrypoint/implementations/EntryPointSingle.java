package es.urjc.ia.bikesurbanfleets.usersgenerator.entrypoint.implementations;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.usersgenerator.SingleUser;
import es.urjc.ia.bikesurbanfleets.usersgenerator.UserProperties;
import es.urjc.ia.bikesurbanfleets.usersgenerator.entrypoint.EntryPoint;
import es.urjc.ia.bikesurbanfleets.usersgenerator.entrypoint.EntryPointType;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a unic user who appears at a specific position.
 * @author IAgroup
 *
 */
@EntryPointType("SINGLEUSER")
public class EntryPointSingle extends EntryPoint {
    /**
     * It is the point where user appears, i. e., where user is located after being generated.
     */
    private GeoPoint positionAppearance;
    
    /**
     * It is the point where user wants to go to.
     */
    private GeoPoint destinationPlace;
    /**
     * Type of user that will be generated.
     */
    private UserProperties userType;
    
    /**
     * It is the time instant when user appears at the system.
     */
    private int timeInstant; 
    
    public EntryPointSingle(GeoPoint positionAppearance, GeoPoint destinationPlace, UserProperties userType, int instant) {
        this.entryPointType = this.getClass().getAnnotation(EntryPointType.class).value();
        this.positionAppearance = positionAppearance;
        this.userType = userType;
        this.timeInstant = instant;
        this.destinationPlace=destinationPlace;
    }

    @Override
    public List<SingleUser> generateUsers() {
        List<SingleUser> singleUsers = new ArrayList<>();
        SingleUser user = new SingleUser(positionAppearance, destinationPlace, userType, timeInstant);
        singleUsers.add(user);
        return singleUsers;
    }
    
    public String toString() {
        String result = positionAppearance.toString();
         result += "| " + destinationPlace+ " \n";
       
        result += "| SINGLE user \n";
        result += "user Type: " + userType + "\n";
        result += "Instant: " + timeInstant + "\n";
        return result;
    }
}
