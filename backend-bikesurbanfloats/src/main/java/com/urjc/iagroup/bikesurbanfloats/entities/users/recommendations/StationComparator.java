package com.urjc.iagroup.bikesurbanfloats.entities.users.recommendations;

import java.util.Comparator;

import com.urjc.iagroup.bikesurbanfloats.entities.Station;

public class StationComparator {
	Comparator<Station> byNumberOfBikes = (s1, s2) -> Integer.compare(s1.availableBikes(), s2.availableBikes());
	Comparator<Station> byNumberOfSlots = (s1, s2) -> Integer.compare(s1.availableSlots(), s2.availableSlots());
/*	Comparator<Integer> byLinearDistance = (s1, s2, u) -> Integer.compare(s1.getPosition().distanceTo(u.getPosition()), s2.getPosition().distanceTo(u.getPosition()); */
}
	