package es.urjc.ia.bikesurbanfleets.core.config;

import com.google.gson.annotations.JsonAdapter;
import es.urjc.ia.bikesurbanfleets.common.util.BoundingBox;
import es.urjc.ia.bikesurbanfleets.core.config.deserializers.EntryPointDeserializer;
import es.urjc.ia.bikesurbanfleets.core.config.deserializers.StationDeserializer;
import es.urjc.ia.bikesurbanfleets.core.config.entrypoints.EntryPoint;
import es.urjc.ia.bikesurbanfleets.core.entities.Station;

import java.util.List;
/**
 * It encapsulates the information of the configuration file in a data type understandable 
 * and manageable for the system.
 * @author IAgroup
 *
 */
public class SimulationConfiguration {
 /**
  * It is the time period during a reservation is valid or active.     
  */
    private int reservationTime;
    
    /**
     * It is the moment when the system stops creating entry points for the simulation, so it ends.   
     */
    private int totalSimulationTime;
    
    /**
     * It is the seed which initializes the random instance. 
     */
    private long randomSeed;
    
    /**
     * It is the absolute route of the used map.  
     */
    private String map;
    
    /**
     * It delimits the simulation area.
     */
    private BoundingBox boundingBox;
    
    /*
     * Path  where history files stored
     */
    private String historyOutputPath;
    
 /**
  * They are all the entry points of the system obtained from the configuration file.
  */
    @JsonAdapter(EntryPointDeserializer.class)
    private List<EntryPoint> entryPoints;

    /**
     * They are all the stations of the system obtained from the configuration file. 
     */
    @JsonAdapter(StationDeserializer.class)
    private List<Station> stations;
    
    public int getReservationTime() {
        return reservationTime;
    }

    public int getTotalSimulationTime() {
        return totalSimulationTime;
    }

    public long getRandomSeed() {
        return randomSeed;
    }

    public String getMap() {
        return map;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public List<EntryPoint> getEntryPoints() {
        return entryPoints;
    }

    public List<Station> getStations() {
        return stations;
    }
    
    public String getOutputPath() {
        return historyOutputPath;
    }
    
}
