package com.urjc.iagroup.bikesurbanfloats.config;

import java.util.ArrayList;
import java.util.Random;

import com.urjc.iagroup.bikesurbanfloats.entities.Station;

public class SystemInfo {
	
	public static ArrayList<Station> stations;
	public static ArrayList<EntryPoint> entryPoints;
	public static int reservationTime = 0;
	public static int totalTimeSimulation = 0;
	public static long randomSeed = 0;
	public static Random random = null;

}
