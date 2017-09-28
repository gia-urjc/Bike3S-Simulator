package com.urjc.iagroup.bikesurbanfloats.entities.personfactory;

import com.urjc.iagroup.bikesurbanfloats.entities.Person;
import com.urjc.iagroup.bikesurbanfloats.entities.PersonTest;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;

public class PersonFactory {

	public Person createPerson(PersonType type, GeoPoint position) {
		switch(type) {
			case PersonTest: return new PersonTest(position);
		}
		throw new IllegalArgumentException("The type" + type + "doesn't exists");
	}
}
