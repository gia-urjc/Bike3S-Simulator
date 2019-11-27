package es.urjc.ia.bikesurbanfleets.usersgenerator;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;

public class SingleUser {

    /**
     * It is the point where user appears, i. e., where user is located after being generated.
     */
    private GeoPoint position;

    /**
     * Destination place of the user
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
    
    
    /**
     * It is the cycling velocity of the user
     */
    protected double cyclingVelocity;

    /**
     * It is the walking velocity of the user
     */
    protected double walkingVelocity;


    @Override
    public String toString() {
        return "{" +
            ", position='" + getPosition() + "'" +
            ", destinationPlace='" + getDestinationPlace() + "'" +
            ", userType='" + getUserType() + "'" +
            ", timeInstant='" + getTimeInstant() + "'" +
            ", cyclingVelocity='" + getCyclingVelocity() + "'" +
            ", walkingVelocity='" + getWalkingVelocity() + "'" +
            "}";
    }

    public SingleUser(GeoPoint position, GeoPoint destinationPlace, UserProperties userType, int instant, 
    		          double cyclingVelocity, double walkingVelocity) {
        this.position = position;
        this.userType = userType;
        this.timeInstant = instant;
        this.destinationPlace = destinationPlace;
        this.cyclingVelocity = cyclingVelocity;
        this.walkingVelocity = walkingVelocity;
    }

    public GeoPoint getPosition() {
        return position;
    }

    public GeoPoint getDestinationPlace() {
        return destinationPlace;
    }


    public UserProperties getUserType() {
        return userType;
    }

    public int getTimeInstant() {
        return timeInstant;
    }

    public double getCyclingVelocity() {
        return cyclingVelocity;
    }

    public double getWalkingVelocity() {
        return walkingVelocity;
    }

}
