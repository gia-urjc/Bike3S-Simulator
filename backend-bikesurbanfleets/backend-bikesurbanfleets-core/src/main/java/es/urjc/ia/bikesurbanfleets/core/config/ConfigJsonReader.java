package es.urjc.ia.bikesurbanfleets.core.config;

import com.google.gson.Gson;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.InfraestructureManager;

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
}
