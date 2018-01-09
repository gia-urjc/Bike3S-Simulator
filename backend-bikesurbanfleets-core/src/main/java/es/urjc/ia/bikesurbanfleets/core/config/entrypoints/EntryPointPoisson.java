package es.urjc.ia.bikesurbanfleets.core.config.entrypoints;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.util.BoundingCircle;
import es.urjc.ia.bikesurbanfleets.common.util.SimulationRandom;
import es.urjc.ia.bikesurbanfleets.common.util.TimeRange;
import es.urjc.ia.bikesurbanfleets.core.config.entrypoints.distributions.DistributionPoisson;
import es.urjc.ia.bikesurbanfleets.core.entities.users.User;
import es.urjc.ia.bikesurbanfleets.core.entities.users.UserFactory;
import es.urjc.ia.bikesurbanfleets.core.events.EventUserAppears;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents several users that appears at system with a Poisson distribution, taking as a reference to generate them the same entry point.
 * It provides a method which generates a variable set of users.
 * Number of users that are generated depends on the value of parameter of followed distribution.
 * @author IAgroup
 */
public class EntryPointPoisson extends EntryPoint {

    /**
     * If a radius is given, position is the center of circle
     * In other case, position is the specific point where user appears
     */
    private GeoPoint position;
    
    /**
     * It is the radius of circle is going to be used to delimit area where users appears
     */
    private double radius;
    
    /**
     * Type of distribution that users generation will follow
     */
    private DistributionPoisson distribution;
    
    /**
     * Type of users that will be generated
     */
    private UserProperties userType;

    /**
     * It is the range of time within which users can appears, i. e.,
     */
    private TimeRange timeRange;
    
    /**
     * It is the number of users that will be generated.
     */
    private int totalUsers;

    @Override
    public List<EventUserAppears> generateEvents() {
        List<EventUserAppears> generatedEvents = new ArrayList<>();
        UserFactory userFactory = new UserFactory();
        int currentTime, endTime;
        int usersCounter = 0;
        int maximumUsers;
        
        if (timeRange == null) {
        	currentTime = 0;
        	endTime = TOTAL_SIMULATION_TIME;
        }
        else {
        	currentTime = timeRange.getStart();
        	endTime = timeRange.getEnd();
        }

        maximumUsers = totalUsers == 0 ? Integer.MAX_VALUE : totalUsers;
        
        while (currentTime < endTime && usersCounter < maximumUsers) {
            User user = userFactory.createUser(userType);
            usersCounter++;
            GeoPoint userPosition;
            
            //If not radius is specified, user just appears in the position submitted.
            if (radius > 0) {
                BoundingCircle boundingCircle = new BoundingCircle(position, radius);
                userPosition = boundingCircle.randomPointInCircle(SimulationRandom.getUserCreationInstance());
            } else {
                userPosition = position;
            }
            int timeEvent = distribution.randomInterarrivalDelay();
            currentTime += timeEvent;
            EventUserAppears newEvent = new EventUserAppears(currentTime, user, userPosition);
            generatedEvents.add(newEvent);
        }
        return generatedEvents;
    }

    @Override
    public String toString() {
        String result = position.toString();
        result += "| Distribution " + distribution.getDistribution();
        result += "| distributionParameter " + distribution.getLambda() + "\n";
        result += "user Type: " + userType;
        return result;
    }

}
