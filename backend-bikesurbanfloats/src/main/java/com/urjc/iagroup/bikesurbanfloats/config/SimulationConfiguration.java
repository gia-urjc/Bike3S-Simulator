package com.urjc.iagroup.bikesurbanfloats.config;

import com.urjc.iagroup.bikesurbanfloats.events.EventUserAppears;
import com.urjc.iagroup.bikesurbanfloats.util.BoundingBox;

import java.util.ArrayList;
import java.util.List;

public class SimulationConfiguration {
	
	private String configurationFile = "configuration/config_stations.json";
	
	//System Information
	private List<EventUserAppears> eventUserAppears = new ArrayList<>();

	private int totalTimeSimulation = 0;
	private long randomSeed = 0;
	
	//GrapHopper Properties
	private String mapDirectory;
	private String GraphHopperLocale;
	
	//Utils
	private BoundingBox boundingBox = null;

	SimulationConfiguration() {}

	public int getTotalTimeSimulation() {
		return totalTimeSimulation;
	}

	public void setTotalTimeSimulation(int totalTimeSimulation) {
		this.totalTimeSimulation = totalTimeSimulation;
	}

	public long getRandomSeed() {
		return randomSeed;
	}

	public void setRandomSeed(long randomSeed) {
		this.randomSeed = randomSeed;
	}

	public BoundingBox getBoundingBox() {
		return boundingBox;
	}

	public void setBoundingBox(BoundingBox boundingBox) {
		this.boundingBox = boundingBox;
	}
	
	public List<EventUserAppears> getEventUserAppears() {
		return eventUserAppears;
	}

	public void setEventUserAppears(List<EventUserAppears> eventUserAppears) {
		this.eventUserAppears = eventUserAppears;
	}

	public String getConfigurationFile() {
		return configurationFile;
	}

	public void setConfigurationFile(String configurationFile) {
		this.configurationFile = configurationFile;
	}
	
	public String getMapDirectory() {
		return mapDirectory;
	}

	public void setMapDirectory(String mapDirectory) {
		this.mapDirectory = mapDirectory;
	}

	public String getGraphHopperLocale() {
		return GraphHopperLocale;
	}

	public void setGraphHopperLocale(String graphHopperLocale) {
		GraphHopperLocale = graphHopperLocale;
	}
	
	public void resetInfo() {
		eventUserAppears = new ArrayList<>();
		totalTimeSimulation = 0;
		randomSeed = 0;
		boundingBox = null;
	}
	
	public String toString() {
		String result = "";
		result += "Entry Points: \n ------------- \n";
		for(EventUserAppears e: eventUserAppears) {
			result += e.toString() + "\n";
		}
		result += "Total time simulation: " + totalTimeSimulation + "\n";
		result += "Random seed: " + randomSeed + "\n";
		return result;
	}

}
