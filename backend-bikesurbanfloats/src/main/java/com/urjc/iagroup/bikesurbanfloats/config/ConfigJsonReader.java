package com.urjc.iagroup.bikesurbanfloats.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;

public class ConfigJsonReader {
	
	private ConfigInfo configInfo = new ConfigInfo();
	private String configFile;
	
	public ConfigJsonReader(String configFile) {
		this.configFile = configFile;
	}

	public ConfigInfo getConfigInfo() {
		return configInfo;
	}

	public void setConfigInfo(ConfigInfo configInfo) {
		this.configInfo = configInfo;
	}
	
	public ConfigInfo readJson() throws FileNotFoundException {
		
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Station.class, new StationDeserializer());
		Gson gson = gsonBuilder.create();
		FileInputStream inputStreamJson = new FileInputStream(new File(configFile));
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStreamJson));
		this.configInfo = gson.fromJson(bufferedReader, ConfigInfo.class);
		 
		return this.configInfo;
	}
	
	

}
