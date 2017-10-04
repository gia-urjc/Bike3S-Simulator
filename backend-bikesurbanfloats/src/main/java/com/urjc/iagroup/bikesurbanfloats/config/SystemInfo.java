package com.urjc.iagroup.bikesurbanfloats.config;

import java.util.ArrayList;
import java.util.Random;

import com.urjc.iagroup.bikesurbanfloats.config.entrypoints.EntryPoint;
import com.urjc.iagroup.bikesurbanfloats.entities.Bike;
import com.urjc.iagroup.bikesurbanfloats.entities.Person;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;

public class SystemInfo {
	
	public static ArrayList<Station> stations;
	public static ArrayList<EntryPoint> entryPoints;
	public static ArrayList<Bike> bikes;
	public static ArrayList<Person> persons;
	public static int reservationTime = 0;
	public static int totalTimeSimulation = 0;
	public static long randomSeed = 0;
	public static Random random = null;
	
	public static String strInfo() {
		String result = "";
		result += "STATIONS: \n ------------- \n";
		for(Station s: SystemInfo.stations) {
			result += s.toString() + "\n";
		}
		result += "Entry Points: \n ------------- \n";
		for(EntryPoint e: SystemInfo.entryPoints) {
			result += e.toString() + "\n";
		}
		result += "Reservation time: " + reservationTime + "\n";
		result += "Total time simulation: " + totalTimeSimulation + "\n";
		result += "Random seed: " + randomSeed + "\n";
		return result;
	}

}
