package com.urjc.iagroup.bikesurbanfloats.entities.users;

import com.urjc.iagroup.bikesurbanfloats.entities.users.UserType;
import com.urjc.iagroup.bikesurbanfloats.entities.users.types.UserFacts;
import com.urjc.iagroup.bikesurbanfloats.entities.users.types.UserRandom;

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
            case USER_FACTS:
            	return new UserFacts();
        }
        throw new IllegalArgumentException("The type" + type + "doesn't exists");
    }
}
