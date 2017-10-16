package com.urjc.iagroup.bikesurbanfloats.config;

import java.util.ArrayList;
import com.urjc.iagroup.bikesurbanfloats.config.entrypoints.EntryPoint;
import com.urjc.iagroup.bikesurbanfloats.entities.Bike;
import com.urjc.iagroup.bikesurbanfloats.entities.Person;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.util.BoundaryRectangle;
import com.urjc.iagroup.bikesurbanfloats.util.RandomUtil;

public class SystemInfo {
	
	public static ArrayList<Station> stations = new ArrayList<>();
	public static ArrayList<EntryPoint> entryPoints = new ArrayList<>();
	public static ArrayList<Bike> bikes = new ArrayList<>();
	public static ArrayList<Person> persons = new ArrayList<>();
	
	public static int reservationTime = 0;
	public static int totalTimeSimulation = 0;
	public static long randomSeed = 0;
	public static RandomUtil random = null;
	
	public static BoundaryRectangle rectangle = null;
	
	public static void resetInfo() {
		stations = new ArrayList<>();
		entryPoints = new ArrayList<>();
		bikes = new ArrayList<>();
		persons = new ArrayList<>();
		reservationTime = 0;
		totalTimeSimulation = 0;
		randomSeed = 0;
		random = null;
		rectangle = null;
	}
	
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
