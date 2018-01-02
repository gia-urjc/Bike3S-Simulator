package es.urjc.ia.bikesurbanfleets.config;

import com.google.gson.Gson;
import es.urjc.ia.bikesurbanfleets.config.entrypoints.EntryPoint;
import es.urjc.ia.bikesurbanfleets.core.SystemManager;
import es.urjc.ia.bikesurbanfleets.entities.Reservation;
import es.urjc.ia.bikesurbanfleets.util.SimulationRandom;

import java.io.FileReader;
import java.io.IOException;

/**
 * This class is used to create, from configuration file, the system's internal classes 
 * necessary to manage the system configuration. 
 * @author IAgroup
 *
 */
public class ConfigJsonReader {

    private String configurationFile;

    private Gson gson;

    public ConfigJsonReader(String configurationFile) {
        this.configurationFile = configurationFile;
        this.gson = new Gson();
    }
    
    /**
     * It creates a simulation configuration object from json configuration file.
     * @return the created simulationo configuration object.
     */
    public SimulationConfiguration createSimulationConfiguration() throws IOException {
        try (FileReader reader = new FileReader(configurationFile)) {
            SimulationConfiguration simulationConfiguration = gson.fromJson(reader, SimulationConfiguration.class);

            SimulationRandom.init(simulationConfiguration.getRandomSeed());
            EntryPoint.TOTAL_SIMULATION_TIME = simulationConfiguration.getTotalSimulationTime();
            Reservation.VALID_TIME = simulationConfiguration.getReservationTime();

            return simulationConfiguration;
        }
    }
    
    /**
     * It creates a system manager object from the simulation configuration object.
     * @return the created system manager object.
     */
    public SystemManager createSystemManager(SimulationConfiguration simulationConfiguration) throws IOException {
        return new SystemManager(simulationConfiguration);
    }

}
