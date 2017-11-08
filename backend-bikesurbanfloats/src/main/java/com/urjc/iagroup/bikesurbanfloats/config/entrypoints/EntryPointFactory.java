package com.urjc.iagroup.bikesurbanfloats.config.entrypoints;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.urjc.iagroup.bikesurbanfloats.config.entrypoints.EntryPoint;
import com.urjc.iagroup.bikesurbanfloats.config.entrypoints.EntryPointPoisson;
import com.urjc.iagroup.bikesurbanfloats.config.entrypoints.EntryPointSingle;
import com.urjc.iagroup.bikesurbanfloats.config.entrypoints.distributions.Distribution.DistributionType;;

/**
 * This class serves to create, in a generic way, entry point instances. 
 * @author IAgroup
 *
 */
public class EntryPointFactory {
	
	private Gson gson;
	
	public EntryPointFactory() {
		this.gson = new Gson();
	}
	
	/**
	 * It creates an entry point of an specific type. 
	 * @param json it conatins the entry point information. 
	 * @param distribution it is the distribution type which determines the entry point type to create.
	 * @return an specific entry point instance.  
	 */
	public EntryPoint createEntryPoint(JsonObject json, DistributionType distribution) {
		switch(distribution) {
			case POISSON: return gson.fromJson(json, EntryPointPoisson.class);
			case NONEDISTRIBUTION: return gson.fromJson(json, EntryPointSingle.class); 
			default: throw new JsonParseException("Type of EntryPoint doesn't exists");
		}
	}

}
