package es.urjc.ia.bikesurbanfleets.core.entities.users;

import com.google.gson.Gson;
import es.urjc.ia.bikesurbanfleets.core.entities.users.types.*;
import es.urjc.ia.bikesurbanfleets.usersgenerator.config.UserProperties;

/**
 * This class serves to create, in a generic way, user instances.
 * @author IAgroup
 */
public class UserFactory {

    Gson gson = new Gson();
    /**
     * It creates a specific type of user.
     * @param epUserProps It is the user type and parameters which determines the instance type to create.
     * @return an instance of a specific user type.
     */
    public User createUser(UserProperties epUserProps) {
        switch (UserType.valueOf(epUserProps.getTypeName())) {
            case USER_RANDOM:
                return new UserRandom();
            case USER_TOURIST:
                return new UserTourist(gson.fromJson(epUserProps.getParameters(),
                        UserTourist.UserTouristParameters.class));
            case USER_EMPLOYEE:
                return new UserEmployee(gson.fromJson(epUserProps.getParameters(),
                        UserEmployee.UserEmployeeParameters.class));
            case USER_STATIONS_BALANCER:
                return new UserStationsBalancer(gson.fromJson(epUserProps.getParameters(),
                        UserStationsBalancer.UserStationsBalancerParameters.class));
            case USER_REASONABLE:
                return new UserReasonable(gson.fromJson(epUserProps.getParameters(),
                        UserReasonable.UserReasonableParameters.class));
            case USER_DISTANCE_RESTRICTION:
                return new UserDistanceRestriction(gson.fromJson(epUserProps.getParameters(),
                        UserDistanceRestriction.UserDistanceRestrictionParameters.class));
            
        }
        throw new IllegalArgumentException("The type" + epUserProps.getTypeName() + "doesn't exists");
    }
}
