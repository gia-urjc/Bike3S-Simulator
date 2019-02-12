package es.urjc.ia.bikesurbanfleets.worldentities.infraestructure;

import es.urjc.ia.bikesurbanfleets.common.demand.DemandManager;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.util.BoundingBox;
import es.urjc.ia.bikesurbanfleets.common.util.BoundingCircle;
import es.urjc.ia.bikesurbanfleets.common.util.SimpleRandom;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Bike;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Station;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.stream.Collectors;

/**
 * This class contains all the information of all the entities at the system.
 * It provides all the usable methods by the user at the system.
 * @author IAgroup
 */
public class InfraestructureManager {

    /**
     * These are all the stations at the system.
     */
    private List<Station> stations;
    
    /**
     * These are all the bikes from all stations at the system.
     */
    private List<Bike> bikes;
        
    private int maxStationCapacity;
    private int minStationCapacity;

    private DemandManager demandManager;

    public DemandManager getDemandManager() {
        return demandManager;
    }
    
    public int getMaxStationCapacity() {
        return maxStationCapacity;
    }

    public int getMinStationCapacity() {
        return minStationCapacity;
    }
    /**
     * It represents the map area where simulation is taking place.
     */
    private BoundingBox bbox;
    
    
    public InfraestructureManager(List<Station> stations, BoundingBox bbox, DemandManager demandManager) throws IOException {
        this.stations = stations;
        this.demandManager= demandManager;
        this.bikes = stations.stream().map(Station::getSlots).flatMap(List::stream).filter(Objects::nonNull).collect(Collectors.toList());
        OptionalInt i =stations.stream().mapToInt(Station::getCapacity).max();
        if (!i.isPresent()) throw new RuntimeException("invalid program state: no stations");
        maxStationCapacity=i.getAsInt();
        i =stations.stream().mapToInt(Station::getCapacity).min();
        if (!i.isPresent()) throw new RuntimeException("invalid program state: no stations");
        minStationCapacity=i.getAsInt();      
        this.bbox = bbox;
    }
    

    public List<Station> consultStations() {
        return stations;
    } 
    
    public List<Bike> consultBikes() {
    	return this.bikes;
    }
 
    //random is a value between 0 and 1
    public GeoPoint generateBoundingBoxRandomPoint(SimpleRandom random) {
        return bbox.randomPoint(random);
    }
    
 //random is a value between 0 and 1
    public GeoPoint generateRandomPointInCircle(GeoPoint center, double radio, SimpleRandom random) {
        BoundingCircle boundingCircle = new BoundingCircle(center, radio);
        return boundingCircle.randomPointInCircle(random);
    }
    

 }
