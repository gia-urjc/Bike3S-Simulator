package com.urjc.iagroup.bikesurbanfloats.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.gson.Gson;

public class ConfigJacksonReader {
	
	private ConfigInfo configInfo = new ConfigInfo();
	private String configFile;
	
	public ConfigJacksonReader(String configFile) {
		this.configFile = configFile;
	}

	public ConfigInfo getConfigInfo() {
		return configInfo;
	}

	public void setConfigInfo(ConfigInfo configInfo) {
		this.configInfo = configInfo;
	}
	
	public ConfigInfo readJson() throws FileNotFoundException {
		
		Gson gson = new Gson();
		FileInputStream inputStreamJson = new FileInputStream(new File(configFile));
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStreamJson));
		this.configInfo = gson.fromJson(bufferedReader, ConfigInfo.class);
		
		System.out.println(configInfo.toString());
		 
		return this.configInfo;
	}
	
	

}
