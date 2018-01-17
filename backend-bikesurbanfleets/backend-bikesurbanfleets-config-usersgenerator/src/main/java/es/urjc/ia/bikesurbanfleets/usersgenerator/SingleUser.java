package es.urjc.ia.bikesurbanfleets.usersgenerator;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;

public class SingleUser {

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

    public SingleUser(GeoPoint position, UserProperties userType, int instant) {
        this.position = position;
        this.userType = userType;
        this.timeInstant = instant;
    }

    public GeoPoint getPosition() {
        return position;
    }

    public UserProperties getUserType() {
        return userType;
    }

    public int getTimeInstant() {
        return timeInstant;
    }
}
