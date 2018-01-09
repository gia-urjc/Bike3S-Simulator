package es.urjc.ia.bikesurbanfleets.usersgenerator.config;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import es.urjc.ia.bikesurbanfleets.common.util.JsonValidation;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ConfigurationIO {

    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private Gson gson;
    private String pathJsonValidator;

    public ConfigurationIO() {
        this.gson = new Gson();
        this.pathJsonValidator = null;
    }

    public ConfigurationIO(String pathSchemaValidator) {
        this.gson = new Gson();
        this.pathJsonValidator = pathSchemaValidator;
    }

    public EntryPointList readPreConfigEntryPoints(String path) throws FileNotFoundException {
        return gson.fromJson(new JsonReader(new FileReader(path)), EntryPointList.class);
    }

    public void writeFinalConfig(String inputConfPath, String outputConfPath, List<SingleUser> users) throws IOException {
        if(pathJsonValidator == null) {
            System.out.println(ANSI_YELLOW + "Warning: You're not using an schema validator, if you generate users not " +
                    "defined in the schema the configuration file will not work" + ANSI_RESET);
        }
        else {
            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(new FileReader(inputConfPath));
            JsonObject confJson = jsonElement.getAsJsonObject();

            confJson.remove("entryPoints");
            JsonArray newEntryPoints = new JsonArray();
            for(SingleUser user: users){
                newEntryPoints.add(gson.toJsonTree(user, SingleUser.class));
            }
            confJson.add("entryPoints", newEntryPoints);
            gson.toJson(users, new FileWriter(outputConfPath));
        }
    }

}
