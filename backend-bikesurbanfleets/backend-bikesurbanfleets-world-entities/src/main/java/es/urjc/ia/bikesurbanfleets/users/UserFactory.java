package es.urjc.ia.bikesurbanfleets.users;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import es.urjc.bikesurbanfleets.services.SimulationServices;
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
    public User createUser(UserProperties epUserProps, SimulationServices services) {

        JsonElement parameters;
        parameters = new JsonObject();
        if(epUserProps.getParameters() != null) {
            parameters = epUserProps.getParameters();
        }
        String type = epUserProps.getTypeName();

        switch (UserType.valueOf(type)) {
            case USER_RANDOM:
                return new UserRandom(services);
            case USER_UNINFORMED:
                return new UserUninformed(services);
            case USER_INFORMED:
                return new UserInformed(gson.fromJson(parameters,
                        UserInformed.UserInformedParameters.class), services);
            case USER_TOURIST:
                return new UserTourist(gson.fromJson(parameters,
                        UserTourist.UserTouristParameters.class), services);
            case USER_COMMUTER:
                return new UserCommuter(gson.fromJson(parameters,
                        UserCommuter.UserEmployeeParameters.class), services);
            case USER_AVAILABLE_RESOURCES: 
                return new UserAvailableResources(gson.fromJson(parameters,
                        UserAvailableResources.UserAvailableResourcesParameters.class), services);
            case USER_REASONABLE:
                return new UserDistanceResourcesRatio(gson.fromJson(parameters,
                        UserDistanceResourcesRatio.UserReasonableParameters.class), services);
            case USER_DISTANCE_RESTRICTION:
                return new UserDistanceRestriction(gson.fromJson(parameters,
                        UserDistanceRestriction.UserDistanceRestrictionParameters.class), services);
            case USER_OBEDIENT:
            	return new UserObedient(gson.fromJson(parameters,
            			UserObedient.UserObedientParameters.class), services);
            
        }
        throw new IllegalArgumentException("The type" + epUserProps.getTypeName() + "doesn't exists");
    }
}
