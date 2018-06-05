package es.urjc.ia.bikesurbanfleets.users;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import es.urjc.ia.bikesurbanfleets.users.UserType;
import es.urjc.ia.bikesurbanfleets.users.types.*;
import es.urjc.ia.bikesurbanfleets.usersgenerator.UserProperties;

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

        JsonElement parameters;
        parameters = new JsonObject();
        if(epUserProps.getParameters() != null) {
            parameters = epUserProps.getParameters();
        }
        String type = epUserProps.getTypeName();

        switch (UserType.valueOf(type)) {
            case USER_RANDOM:
                return new UserRandom();
            case USER_UNINFORMED:
                return new UserUninformed();
            case USER_INFORMED:
                return new UserInformed(gson.fromJson(parameters,
                        UserInformed.UserInformedParameters.class));
            case USER_TOURIST:
                return new UserTourist(gson.fromJson(parameters,
                        UserTourist.UserTouristParameters.class));
            case USER_COMMUTER:
                return new UserCommuter(gson.fromJson(parameters,
                        UserCommuter.UserEmployeeParameters.class));
            case USER_AVAILABLE_RESOURCES: 
                return new UserAvailableResources(gson.fromJson(parameters,
                        UserAvailableResources.UserAvailableResourcesParameters.class));
            case USER_REASONABLE:
                return new UserReasonable(gson.fromJson(parameters,
                        UserReasonable.UserReasonableParameters.class));
            case USER_DISTANCE_RESTRICTION:
                return new UserDistanceRestriction(gson.fromJson(parameters,
                        UserDistanceRestriction.UserDistanceRestrictionParameters.class));
            case USER_OBEDIENT:
            	return new UserObedient(gson.fromJson(parameters,
            			UserObedient.UserObedientParameters.class));
            
        }
        throw new IllegalArgumentException("The type" + epUserProps.getTypeName() + "doesn't exists");
    }
}
