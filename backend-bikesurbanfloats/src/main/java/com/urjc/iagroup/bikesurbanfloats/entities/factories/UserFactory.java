package com.urjc.iagroup.bikesurbanfloats.entities.factories;

import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.entities.User;
import com.urjc.iagroup.bikesurbanfloats.entities.User.UserType;
import com.urjc.iagroup.bikesurbanfloats.entities.UserTest;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;

public class UserFactory {

	public User createUser(int id, UserType type, GeoPoint position, SystemInfo systemInfo) {
		switch(type) {
		case USERTEST: return new UserTest(id, position, systemInfo);
		}
		throw new IllegalArgumentException("The type" + type + "doesn't exists");
	}
}
