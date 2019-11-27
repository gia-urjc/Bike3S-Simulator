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
     * It is the time instant when user appears at the system.
     */
    private int timeInstant;

    @Override
    public List<SingleUser> generateUsers() {
        List<SingleUser> singleUsers = new ArrayList<>();
        SingleUser user = new SingleUser(positionAppearance, destinationPlace, userType, timeInstant, cyclingVelocity, walkingVelocity);
        singleUsers.add(user);
        return singleUsers;
    }

    @Override
    public String toString() {
        return super.toString() + "\n" + "EntryPointSingle{" +
                "timeInstant=" + timeInstant +
                '}';
    }
}
