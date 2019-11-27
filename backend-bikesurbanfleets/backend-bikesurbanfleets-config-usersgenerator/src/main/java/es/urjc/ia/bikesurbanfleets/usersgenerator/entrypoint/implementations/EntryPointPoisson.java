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
import java.util.Random;

/**
 * This class represents several users that appears at system with a Poisson distribution, taking as a reference to generate them the same entry point.
 * It provides a method which generates a variable set of users.
 * Number of users that are generated depends on the value of parameter of followed distribution.
 * @author IAgroup
 */
@EntryPointType("POISSON")
public class EntryPointPoisson extends EntryPoint {

    /**
     * Type of distribution that users generation will follow
     */
    private DistributionPoisson distribution;

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
            if (radiusAppears > 0) {
                BoundingCircle boundingCircle = new BoundingCircle(positionAppearance, radiusAppears);
                userPosition = boundingCircle.randomPointInCircle(SimulationRandom.getInstance());
            } else {
                userPosition = positionAppearance;
            }
            //If not radius is specified, user goes to the position submitted.
            if (radiusGoTo > 0) {
                BoundingCircle boundingCircle = new BoundingCircle(destinationPlace, radiusGoTo);
                userGoTo = boundingCircle.randomPointInCircle(SimulationRandom.getInstance());
            } else {
                userGoTo = destinationPlace;
            }
            int timeEvent = distribution.randomInterarrivalDelay();
            currentTime += timeEvent;
            
            // generate random cycling and walking velocities
            // numbers follow Normal distribution N(value,0.1) 
            Random r = new Random();
            
            double randomCyclingVel = Math.max(0.1, r.nextGaussian()*0.1 + cyclingVelocity);
            double randomWalkingVel = Math.max(0.1, r.nextGaussian()*0.1 + walkingVelocity);
            
            SingleUser user = new SingleUser(userPosition, userGoTo, userType, currentTime, randomCyclingVel, randomWalkingVel);
            users.add(user);
        }
        return users;
    }

    @Override
    public String toString() {
        return super.toString() + "\n" + "EntryPointPoisson{" +
                "distribution=" + distribution +
                ", timeRange=" + timeRange +
                ", totalUsers=" + totalUsers +
                '}';
    }
}
