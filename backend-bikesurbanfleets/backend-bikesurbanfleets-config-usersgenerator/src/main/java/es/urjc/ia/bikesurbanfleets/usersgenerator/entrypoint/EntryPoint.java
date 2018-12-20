package es.urjc.ia.bikesurbanfleets.usersgenerator.entrypoint;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.usersgenerator.SingleUser;
import es.urjc.ia.bikesurbanfleets.usersgenerator.UserProperties;

import java.util.List;

/**
 * It is an event generator for user appearances.
 * It represents an entry point at system geographic map where a unique user or several users
 * appear and start interacting with the system.
 * @author IAgroup
 *
 */
public abstract class EntryPoint {

    /**
     * If a radius is given, position is the center of circle
     * In other case, position is the specific point where user appears
     */
    protected GeoPoint positionAppearance;

    /**
     * It is the point where user wants to go to.
     */
    protected GeoPoint destinationPlace;

    /**
     * Type of users that will be generated
     */
    protected UserProperties userType;

    /**
     * It is the radius of circle is going to be used to delimit area where users appears
     */
    protected double radiusAppears;

    /**
     * It is the radius of circle is going to be used to delimit area where users wants to go to
     */
    protected double radiusGoTo;

    protected String entryPointType;

    public static int TOTAL_SIMULATION_TIME;
    /**
     * It generate single users for the configuration file,
     * which are the main events that starts the simulation execution.
     * @return a list of single users
     */

    public String getEntryPointType() {
        return entryPointType;
    }

    public abstract List<SingleUser> generateUsers();

    @Override
    public String toString() {
        return "EntryPoint{" +
                "positionAppearance=" + positionAppearance +
                ", destinationPlace=" + destinationPlace +
                ", userType=" + userType +
                ", radiusAppears=" + radiusAppears +
                ", radiusGoTo=" + radiusGoTo +
                ", entryPointType='" + entryPointType + '\'' +
                '}';
    }
}