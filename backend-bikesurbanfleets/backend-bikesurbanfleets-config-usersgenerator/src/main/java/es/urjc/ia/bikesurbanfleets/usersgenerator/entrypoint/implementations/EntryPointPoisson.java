package es.urjc.ia.bikesurbanfleets.usersgenerator.entrypoint.implementations;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.util.BoundingCircle;
import es.urjc.ia.bikesurbanfleets.common.util.SimulationRandom;
import es.urjc.ia.bikesurbanfleets.common.util.TimeRange;
import es.urjc.ia.bikesurbanfleets.usersgenerator.SingleUser;
import es.urjc.ia.bikesurbanfleets.usersgenerator.UserProperties;
import es.urjc.ia.bikesurbanfleets.usersgenerator.entrypoint.EntryPoint;
import es.urjc.ia.bikesurbanfleets.usersgenerator.entrypoint.EntryPointType;
import es.urjc.ia.bikesurbanfleets.usersgenerator.entrypoint.distributions.DistributionPoisson;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents several users that appears at system with a Poisson distribution, taking as a reference to generate them the same entry point.
 * It provides a method which generates a variable set of users.
 * Number of users that are generated depends on the value of parameter of followed distribution.
 * @author IAgroup
 */
@EntryPointType("POISSON")
public class EntryPointPoisson extends EntryPoint {

    /**
     * If a radius is given, position is the center of circle
     * In other case, position is the specific point where user appears
     */
    private GeoPoint positionappearance;

    /**
     * It is the point where user wants to go to.
     */
    private GeoPoint destinationPlace;
 
    /**
     * It is the radius of circle is going to be used to delimit area where users appears
     */
    private double radiusappears;

    /**
     * It is the radius of circle is going to be used to delimit area where users wants to go to
     */
    private double radiusgoto;
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
    public List<SingleUser> generateUsers() {
        List<SingleUser> users = new ArrayList<>();
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
            usersCounter++;
            GeoPoint userPosition,userGoTo;

            //If not radius is specified, user just appears in the position submitted.
            if (radiusappears > 0) {
                BoundingCircle boundingCircle = new BoundingCircle(positionappearance, radiusappears);
                userPosition = boundingCircle.randomPointInCircle(SimulationRandom.getInstance());
            } else {
                userPosition = positionappearance;
            }
            //If not radius is specified, user goes to the position submitted.
            if (radiusgoto > 0) {
                BoundingCircle boundingCircle = new BoundingCircle(destinationPlace, radiusgoto);
                userGoTo = boundingCircle.randomPointInCircle(SimulationRandom.getInstance());
            } else {
                userGoTo = destinationPlace;
            }
            int timeEvent = distribution.randomInterarrivalDelay();
            currentTime += timeEvent;
            SingleUser user = new SingleUser(userPosition, userGoTo, userType, currentTime);
            users.add(user);
        }
        return users;
    }

    @Override
    public String toString() {
        String result = positionappearance.toString();
        result += "| " + destinationPlace.toString() + " \n";
        result += "| EntryPointType" + this.getEntryPointType();
        result += "| distributionParameter " + distribution.getLambda() + "\n";
        result += "user Type: " + userType;
        return result;
    }

}
