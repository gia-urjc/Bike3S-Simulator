package es.urjc.ia.bikesurbanfleets.core.config;

import com.google.gson.Gson;
import es.urjc.ia.bikesurbanfleets.common.config.GlobalInfo;
import es.urjc.ia.bikesurbanfleets.common.util.SimulationRandom;
import es.urjc.ia.bikesurbanfleets.infraestructure.InfraestructureManager;

import java.io.FileReader;
import java.io.IOException;

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
        try (FileReader reader = new FileReader(globalConfFile)) {
            GlobalInfo globalInfo = gson.fromJson(reader, GlobalInfo.class);
            if(globalInfo.getRandomSeed() == 0) {
                SimulationRandom.init();
            }
            else {
                SimulationRandom.init(globalInfo.getRandomSeed());
            }
            return globalInfo;
        }
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

    /**
     * It creates a system manager object from the simulation configuration object.
     * @return the created system manager object.
     */
    public InfraestructureManager createInfraestructureManager(StationsConfig stationsInfo, GlobalInfo globalInfo) throws IOException {
        return new InfraestructureManager(stationsInfo.getStations(), globalInfo.getBoundingBox());

    }

}
