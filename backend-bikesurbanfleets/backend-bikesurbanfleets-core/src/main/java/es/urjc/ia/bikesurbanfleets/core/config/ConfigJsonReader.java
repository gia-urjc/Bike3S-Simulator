package es.urjc.ia.bikesurbanfleets.core.config;

import com.google.gson.Gson;
import es.urjc.ia.bikesurbanfleets.common.util.SimulationRandom;
import es.urjc.ia.bikesurbanfleets.systemmanager.SystemManager;

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
            SimulationRandom.init(globalInfo.getRandomSeed());
            return globalInfo;
        }
    }

    public StationsInfo readStationsConfiguration() throws IOException {
        try (FileReader reader = new FileReader(stationConfFile)) {
            StationsInfo stationsInfo = gson.fromJson(reader, StationsInfo.class);
            return stationsInfo;
        }
    }

    public UsersInfo readUsersConfiguration() throws IOException {
        try (FileReader reader = new FileReader(usersConfFile)) {
            UsersInfo usersInfo = gson.fromJson(reader, UsersInfo.class);
            return usersInfo;
        }
    }

    /**
     * It creates a system manager object from the simulation configuration object.
     * @return the created system manager object.
     */
    public SystemManager createSystemManager(StationsInfo stationsInfo, GlobalInfo globalInfo) throws IOException {
        return new SystemManager(stationsInfo.getStations(), globalInfo.getMap(), globalInfo.getBoundingBox());
    }

}
