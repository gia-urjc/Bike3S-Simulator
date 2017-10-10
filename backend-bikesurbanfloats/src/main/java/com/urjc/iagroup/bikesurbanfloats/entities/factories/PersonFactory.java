package com.urjc.iagroup.bikesurbanfloats.entities.factories;

import com.urjc.iagroup.bikesurbanfloats.entities.PersonSpecificBehaviour;
import com.urjc.iagroup.bikesurbanfloats.entities.PersonTest;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.util.PersonType;

public class PersonFactory {

	public PersonSpecificBehaviour createPerson(int id, PersonType type, GeoPoint position) {
		switch(type) {
			case PersonTest: return new PersonTest(id, position);
		}
		throw new IllegalArgumentException("The type" + type + "doesn't exists");
	}
}
