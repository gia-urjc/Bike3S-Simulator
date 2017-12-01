package com.urjc.iagroup.bikesurbanfloats.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

public class JsonValidation {
	
	
	public static String validate(String schemaDir, String jsonDir, String jsValidatorDir) throws IOException, InterruptedException {
		
		Gson gson = new Gson();
		JsonParser parser = new JsonParser();
		
		ArrayList<String> command = new ArrayList<>();
		command.addAll(Arrays.asList("node", jsValidatorDir, "-i", jsonDir, "-s", schemaDir));
		ProcessBuilder pb = new ProcessBuilder(command);
		Process validationProcess = pb.start();
		BufferedReader in = new BufferedReader(new InputStreamReader(validationProcess.getInputStream()));
		String line;
		String output = "";
		while ((line = in.readLine()) != null) {
			output += line;
		}
		validationProcess.waitFor();
		return output;
	}

}
