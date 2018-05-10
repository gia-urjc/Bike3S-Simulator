package es.urjc.ia.bikesurbanfleets.usersgenerator;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import es.urjc.ia.bikesurbanfleets.common.config.GlobalInfo;
import es.urjc.ia.bikesurbanfleets.common.util.JsonValidation;
import es.urjc.ia.bikesurbanfleets.common.util.JsonValidation.ValidationParams;
import es.urjc.ia.bikesurbanfleets.usersgenerator.config.EntryPointInfo;
import es.urjc.ia.bikesurbanfleets.usersgenerator.entrypoint.EntryPoint;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;

public class ConfigurationIO {

    private Gson gson;
    private String pathJsonValidator;
    private String entryPointSchema;
    private String globalConfigSchema;

    public ConfigurationIO() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.pathJsonValidator = null;
        System.out.println("Warning: You're not using an schema validator, if you generate users not " +
                "defined in the schema the configuration file will not work");
    }

    public ConfigurationIO(String pathSchemaValidator, String entryPointSchema, String globalConfigSchema) {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.pathJsonValidator = pathSchemaValidator;
        this.entryPointSchema = entryPointSchema;
        this.globalConfigSchema = globalConfigSchema;
    }

    public EntryPointInfo readPreConfigEntryPoints(String entryPointConfigPath) throws Exception {
        if(pathJsonValidator != null) {
            ValidationParams vParams = new ValidationParams();
            vParams.setSchemaDir(entryPointSchema).setJsonDir(entryPointConfigPath).setJsValidatorDir(pathJsonValidator);
            String resultValidation = JsonValidation.validate(vParams);
            if(!resultValidation.equals("OK")) {
                System.out.println("ValidationInput " + resultValidation);
                throw new Exception(resultValidation);
            }
            System.out.println("Validation Entry Points configuration input: "+ resultValidation);
        }
        return gson.fromJson(new JsonReader(new FileReader(entryPointConfigPath)), EntryPointInfo.class);
    }

    public GlobalInfo readPreConfigGlobalInfo(String globalConfigPath) throws Exception {
        if(pathJsonValidator != null) {
            ValidationParams vParams = new ValidationParams();
            vParams.setSchemaDir(globalConfigSchema).setJsonDir(globalConfigPath).setJsValidatorDir(pathJsonValidator);
            String resultValidation = JsonValidation.validate(vParams);
            if(!resultValidation.equals("OK")) {
                System.out.println("ValidationInput " + resultValidation);
                throw new Exception(resultValidation);
            }
            System.out.println("Validation global configuration input: "+ resultValidation);
        }
        return gson.fromJson(new JsonReader(new FileReader(globalConfigPath)), GlobalInfo.class);
    }

    public void writeFinalConfig(String inputConfPath, String outputConfPath, EntryPointInfo entryPoints) throws Exception {
        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(new FileReader(inputConfPath));
        JsonObject confJson = jsonElement.getAsJsonObject();
        confJson.remove("entryPoints");
        JsonArray initialUsers = new JsonArray();
        for(EntryPoint entryPoint: entryPoints.getEntryPoints()) {
            List<SingleUser> newUsers = entryPoint.generateUsers();
            for(SingleUser user: newUsers) {
                initialUsers.add(gson.toJsonTree(user, SingleUser.class));
            }
        }
        confJson.add("initialUsers", initialUsers);
        FileWriter file = new FileWriter(outputConfPath);
        gson.toJson(confJson, file);
        file.close();
        System.out.println("Configuration created without problems in:");
        System.out.println(outputConfPath);
    }

}
