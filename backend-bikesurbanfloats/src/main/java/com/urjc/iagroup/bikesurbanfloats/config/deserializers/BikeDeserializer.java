package com.urjc.iagroup.bikesurbanfloats.config.deserializers;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.urjc.iagroup.bikesurbanfloats.entities.Bike;
import com.urjc.iagroup.bikesurbanfloats.util.IdGenerator;

public class BikeDeserializer implements JsonDeserializer<Bike>{
	
	private final static String JSON_ATR_RESERVED = "reserved"; 
	
	private IdGenerator bikeIdGen;
	
	public BikeDeserializer(IdGenerator bikeIdGen) { 
		this.bikeIdGen = bikeIdGen;
	}

	@Override
	public Bike deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		
		Bike bike = new Bike();
		boolean reservedBike = json.getAsJsonObject().get(JSON_ATR_RESERVED).getAsBoolean();
		bike.setReserved(reservedBike);
		return bike;
	}
	
	

}
