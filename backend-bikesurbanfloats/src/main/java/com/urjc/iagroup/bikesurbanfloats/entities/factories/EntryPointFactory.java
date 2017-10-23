package com.urjc.iagroup.bikesurbanfloats.entities.factories;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.urjc.iagroup.bikesurbanfloats.config.entrypoints.EntryPoint;
import com.urjc.iagroup.bikesurbanfloats.config.entrypoints.EntryPointPoisson;
import com.urjc.iagroup.bikesurbanfloats.config.entrypoints.EntryPointSingle;
import com.urjc.iagroup.bikesurbanfloats.config.distributions.Distribution.DistributionType;;

public class EntryPointFactory {
	
	private Gson gson;
	
	public EntryPointFactory() {
		this.gson = new Gson();
	}
	
	public EntryPoint createEntryPoint(JsonObject json, DistributionType distribution) {
		switch(distribution) {
			case POISSON: return gson.fromJson(json, EntryPointPoisson.class);
			case NONEDISTRIBUTION: return gson.fromJson(json, EntryPointSingle.class); 
			default: throw new JsonParseException("Type of EntryPoint doesn't exists");
		}
	}

}
