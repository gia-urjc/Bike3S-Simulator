package es.urjc.ia.bikesurbanfleets.usersgenerator.config.entrypoints;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.util.BoundingCircle;
import es.urjc.ia.bikesurbanfleets.common.util.SimulationRandom;
import es.urjc.ia.bikesurbanfleets.common.util.TimeRange;
import es.urjc.ia.bikesurbanfleets.usersgenerator.config.SingleUser;
import es.urjc.ia.bikesurbanfleets.usersgenerator.config.UserProperties;
import es.urjc.ia.bikesurbanfleets.usersgenerator.config.entrypoints.config.distributions.DistributionPoisson;

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
            SingleUser user = new SingleUser(userPosition, userType, currentTime);
            users.add(user);
        }
        return users;
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
