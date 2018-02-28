package es.urjc.ia.bikesurbanfleets.usersgenerator.entrypoint.implementations;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.util.BoundingCircle;
import es.urjc.ia.bikesurbanfleets.common.util.IntegerRange;
import es.urjc.ia.bikesurbanfleets.common.util.SimulationRandom;
import es.urjc.ia.bikesurbanfleets.common.util.TimeRange;
import es.urjc.ia.bikesurbanfleets.usersgenerator.SingleUser;
import es.urjc.ia.bikesurbanfleets.usersgenerator.UserProperties;
import es.urjc.ia.bikesurbanfleets.usersgenerator.UserPropertiesByPercentage;
import es.urjc.ia.bikesurbanfleets.usersgenerator.entrypoint.EntryPoint;
import es.urjc.ia.bikesurbanfleets.usersgenerator.entrypoint.distributions.DistributionPoisson;
import org.apache.commons.lang3.Range;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

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
    private List<UserPropertiesByPercentage> userTypeByPercentage;

    /**
     * It is the range of time within which users can appears, i. e.,
     */
    private TimeRange timeRange;

    /**
     * It is the number of users that will be generated.
     */
    private int totalUsers;

    @Override
    public List<SingleUser> generateUsers() throws IllegalStateException {
        SimulationRandom random = SimulationRandom.getUserCreationInstance();
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


        int sumPercentage = 0;
        List<Integer> percentages = userTypeByPercentage.stream().map(u -> u.getPercentage()).collect(Collectors.toList());
        for(Integer percentage: percentages) {
            sumPercentage += percentage;
        }
        checkPercentages(sumPercentage);

        maximumUsers = totalUsers == 0 ? Integer.MAX_VALUE : totalUsers;

        List<IntegerRange> rangesForPercentages = createRangesOfPercentages(percentages);
        while (currentTime < endTime && usersCounter < maximumUsers) {

            Double rDoubleValue = random.nextDouble() * 100;
            int randomValue = rDoubleValue.intValue();
            UserProperties actualUserType = null;
            for(int i = 0; i < rangesForPercentages.size(); i++) {
                if(rangesForPercentages.get(i).contains(randomValue)){
                    actualUserType = userTypeByPercentage.get(i).getUserType();
                    break;
                }
            }
            usersCounter++;
            GeoPoint userPosition;

            //If not radius is specified, user just appears in the position submitted.
            if (radius > 0) {
                BoundingCircle boundingCircle = new BoundingCircle(position, radius);
                userPosition = boundingCircle.randomPointInCircle(random);
            } else {
                userPosition = position;
            }
            int timeEvent = distribution.randomInterarrivalDelay();
            currentTime += timeEvent;
            SingleUser user = new SingleUser(userPosition, actualUserType, currentTime);
            users.add(user);
            System.out.println(actualUserType.getTypeName());
        }
        return users;
    }

    private void checkPercentages(int sumPercentage) {
        int maximumUsers;
        if(sumPercentage != 100) {
            throw new IllegalStateException("User types percentages are bad set");
        }
    }

    private List<IntegerRange> createRangesOfPercentages(List<Integer> percentages) {
        int sum = 0;
        int previousPercentage = 0;
        List<IntegerRange> resultRanges = new ArrayList<>();
        int minimum = 0;
        int maximum = 100;
        for(Integer percentage: percentages) {
            minimum = sum;
            sum += percentage;
            maximum = sum;
            IntegerRange newRange = new IntegerRange(minimum, maximum);
            resultRanges.add(newRange);
        }

        resultRanges.forEach(r -> System.out.println(r.toString()));
        return resultRanges;
    }

    @Override
    public String toString() {
        return "EntryPointPoisson{" +
                "position=" + position +
                ", radius=" + radius +
                ", distribution=" + distribution +
                ", userTypeByPercentage=" + userTypeByPercentage +
                ", timeRange=" + timeRange +
                ", totalUsers=" + totalUsers +
                '}';
    }
}