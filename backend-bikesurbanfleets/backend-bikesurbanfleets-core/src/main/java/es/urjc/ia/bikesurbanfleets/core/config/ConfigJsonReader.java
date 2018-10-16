package es.urjc.ia.bikesurbanfleets.core.config;

import com.google.gson.Gson;
import es.urjc.ia.bikesurbanfleets.common.config.GlobalInfo;
import es.urjc.ia.bikesurbanfleets.infraestructure.InfraestructureManager;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * This class is used to create, from configuration file, the system's internal classes 
 * necessary to manage the system configuration. 
 * @author IAgroup
 *
 */
public class ConfigJsonReader {

    private String globalConfFile;
    private String stationConfFile;
    private String usersConfFile;

    private Gson gson;

    public ConfigJsonReader(String globalConfFile, String stationsConfFile, String usersConfFile) {
        this.globalConfFile = globalConfFile;
        this.stationConfFile = stationsConfFile;
        this.usersConfFile = usersConfFile;
        this.gson = new Gson();
    }
    
    /**
     * It creates a simulation configuration object from json configuration file.
     * @return the created simulationo configuration object.
     */
    public GlobalInfo readGlobalConfiguration() throws IOException {
        String globalConfigStr = new String(Files.readAllBytes(Paths.get(globalConfFile)), StandardCharsets.UTF_8);
        globalConfigStr = globalConfigStr.replace("\\", "/");
        GlobalInfo globalInfo = gson.fromJson(globalConfigStr, GlobalInfo.class);
        return globalInfo;
    }

    public StationsConfig readStationsConfiguration() throws IOException {
        try (FileReader reader = new FileReader(stationConfFile)) {
            StationsConfig stationsConfig = gson.fromJson(reader, StationsConfig.class);
            return stationsConfig;
        }
    }

    public UsersConfig readUsersConfiguration() throws IOException {
        try (FileReader reader = new FileReader(usersConfFile)) {
            UsersConfig usersConfig = gson.fromJson(reader, UsersConfig.class);
            return usersConfig;
        }
    }
public Message readMessage(JsonReader reader) throws IOException {
     long id = -1;
     String text = null;
     User user = null;
     List<Double> geo = null;

     reader.beginObject();
     while (reader.hasNext()) {
       String name = reader.nextName();
       if (name.equals("id")) {
         id = reader.nextLong();
       } else if (name.equals("text")) {
         text = reader.nextString();
       } else if (name.equals("geo") && reader.peek() != JsonToken.NULL) {
         geo = readDoublesArray(reader);
       } else if (name.equals("user")) {
         user = readUser(reader);
       } else {
         reader.skipValue();
       }
     }
     reader.endObject();
     return new Message(id, text, user, geo);
   }
    /**
     * It creates a system manager object from the simulation configuration object.
     * @return the created system manager object.
     */
    public InfraestructureManager createInfraestructureManager(StationsConfig stationsInfo, GlobalInfo globalInfo) throws IOException {
        return new InfraestructureManager(stationsInfo.getStations(), globalInfo.getBoundingBox());

    }

}
