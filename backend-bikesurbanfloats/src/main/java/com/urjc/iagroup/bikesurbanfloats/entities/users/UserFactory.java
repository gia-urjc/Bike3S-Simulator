package com.urjc.iagroup.bikesurbanfloats.entities.users;

import com.urjc.iagroup.bikesurbanfloats.entities.users.UserType;

import com.urjc.iagroup.bikesurbanfloats.entities.users.types.*;

/**
 * This class serves to create, in a generic way, user instances.
 * @author IAgroup
 */
public class UserFactory {
    /**
     * It creates a specific type of user.
     * @param type It is the user type which determines the instance type to create.
     * @return an instance of a specific user type.
     */
    public User createUser(UserType type) {
        switch (type) {
            case USER_RANDOM:
                return new UserRandom();
            case USER_TOURIST:
            	return new UserTourist();
        case USER_EMPLOYEE:
        	return new UserEmployee();
        case USER_STATIONS_BALANCER:
        	return new UserStationsBalancer();
        case USER_WEIGHER: 
        	return new UserWeigher();
        	
        }
        throw new IllegalArgumentException("The type" + type + "doesn't exists");
    }
}
