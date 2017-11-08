package com.urjc.iagroup.bikesurbanfloats.entities.users;

import com.urjc.iagroup.bikesurbanfloats.entities.User;

public class UserFactory {

    public User createUser(UserType type) {
        switch (type) {
            case USER_TEST:
                return new UserTest();
        }
        throw new IllegalArgumentException("The type" + type + "doesn't exists");
    }
}
