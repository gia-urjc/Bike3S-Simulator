package es.urjc.ia.bikesurbanfleets.usersgenerator;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import es.urjc.ia.bikesurbanfleets.common.util.JsonValidation;
import es.urjc.ia.bikesurbanfleets.usersgenerator.entrypoint.EntryPoint;
import es.urjc.ia.bikesurbanfleets.usersgenerator.entrypoint.implementations.EntryPointSingle;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;

public class ConfigurationIO {

    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_YELLOW = "\u001B[33m";

    private Gson gson;
    private String pathJsonValidator;
    private String pathSchemas;

    public ConfigurationIO() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.pathJsonValidator = null;
        System.out.println(ANSI_YELLOW + "Warning: You're not using an schema validator, if you generate users not " +
                "defined in the schema the configuration file will not work" + ANSI_RESET);
    }

    public ConfigurationIO(String pathSchemaValidator, String pathSchemas) {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.pathJsonValidator = pathSchemaValidator;
        this.pathSchemas = pathSchemas;
    }

    public EntryPointList readPreConfigEntryPoints(String inputConfPath) throws Exception {
        EntryPointList entryPoints;
        if(pathJsonValidator != null){
            String resultValidation = JsonValidation.validate(pathSchemas, inputConfPath, pathJsonValidator);
            if(!resultValidation.equals("OK")) {
                System.out.println(ANSI_GREEN + "ValidationInput " + resultValidation);
                throw new Exception(resultValidation);
            }
        }
        entryPoints = gson.fromJson(new JsonReader(new FileReader(inputConfPath)), EntryPointList.class);
        System.out.println(entryPoints.getEntryPoints().size());
        return gson.fromJson(new JsonReader(new FileReader(inputConfPath)), EntryPointList.class);
    }

    public void writeFinalConfig(String inputConfPath, String outputConfPath, EntryPointList entryPoints) throws Exception {
        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(new FileReader(inputConfPath));
        JsonObject confJson = jsonElement.getAsJsonObject();
        confJson.remove("entryPoints");
        JsonArray newEntryPoints = new JsonArray();
        for(EntryPoint entryPoint: entryPoints.getEntryPoints()) {
            List<SingleUser> newUsers = entryPoint.generateUsers();
            for(SingleUser user: newUsers) {
                EntryPointSingle newEntryPoint = new EntryPointSingle(user.getPosition(), user.getUserType(), user.getTimeInstant());
                newEntryPoints.add(gson.toJsonTree(newEntryPoint, EntryPointSingle.class));
            }
        }
        confJson.add("entryPoints", newEntryPoints);
        FileWriter file = new FileWriter(outputConfPath);
        gson.toJson(confJson, file);
        file.close();

        if(pathJsonValidator != null) {
            String resultValidation = JsonValidation.validate(pathSchemas, outputConfPath, pathJsonValidator);
            if(!resultValidation.equals("OK")){
                System.out.println(ANSI_RED + "Configuration created with errors");
                throw new Exception(resultValidation);
            }
            System.out.println(ANSI_GREEN + "ValidationOutput " + resultValidation + ANSI_RESET);
            System.out.println(ANSI_GREEN + "Configuration created without problems");
        }
    }

}
