package com.urjc.iagroup.bikesurbanfloats.config;

import com.google.gson.annotations.JsonAdapter;
import com.urjc.iagroup.bikesurbanfloats.config.deserializers.EntryPointDeserializer;
import com.urjc.iagroup.bikesurbanfloats.config.deserializers.StationDeserializer;
import com.urjc.iagroup.bikesurbanfloats.config.entrypoints.EntryPoint;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.util.BoundingBox;

import java.util.List;

public class SimulationConfiguration {
	
	private int reservationTime;
	private int totalSimulationTime;
	private long randomSeed;
	private String map;
	private BoundingBox boundingBox;

	@JsonAdapter(EntryPointDeserializer.class)
	private List<EntryPoint> entryPoints;

	@JsonAdapter(StationDeserializer.class)
	private List<Station> stations;

    public int getReservationTime() {
        return reservationTime;
    }

    public int getTotalSimulationTime() {
        return totalSimulationTime;
    }

    public long getRandomSeed() {
        return randomSeed;
    }

    public String getMap() {
        return map;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public List<EntryPoint> getEntryPoints() {
        return entryPoints;
    }

    public List<Station> getStations() {
        return stations;
    }
}
