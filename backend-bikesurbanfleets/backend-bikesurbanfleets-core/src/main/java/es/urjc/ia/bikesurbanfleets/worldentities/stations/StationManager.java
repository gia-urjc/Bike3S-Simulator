package es.urjc.ia.bikesurbanfleets.worldentities.stations;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.simple.StationComparator;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Bike;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.stream.Collectors;

/**
 * This class contains all the information of all the entities at the system. It
 * provides all the usable methods by the user at the system.
 *
 * @author IAgroup
 */
public class StationManager {

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

    public int getMaxStationCapacity() {
        return maxStationCapacity;
    }

    public int getMinStationCapacity() {
        return minStationCapacity;
    }

    public StationManager(List<Station> stations) throws IOException {

        this.stations = stations;
        this.bikes = stations.stream().map(Station::getSlots).flatMap(List::stream).filter(Objects::nonNull).collect(Collectors.toList());
        OptionalInt i = stations.stream().mapToInt(Station::getCapacity).max();
        if (!i.isPresent()) {
            throw new RuntimeException("invalid program state: no stations");
        }
        maxStationCapacity = i.getAsInt();
        i = stations.stream().mapToInt(Station::getCapacity).min();
        if (!i.isPresent()) {
            throw new RuntimeException("invalid program state: no stations");
        }
        minStationCapacity = i.getAsInt();
    }

    
    public class UsageData {

        public int numberStations = 0;
        public int totalNumberBikes = 0;
        public int numberBikesRent = 0;
        public int numberBikesAvailableInStations = 0;
        public int numberSlotsAvailableInStations = 0;
        public int numberBikesReservedInStations = 0;
        public int numberSlotsReservedInStations = 0;
        public int totalCapacity = 0;
        public double maxBikedemand = 0;
        public double minBikedemand = 0;
    }

    public UsageData getGlobalStationCurrentUsagedata() {
        UsageData ud = new UsageData();
        double aux;
        for (Station s : stations) {
            ud.numberBikesAvailableInStations += s.availableBikes();
            ud.numberSlotsAvailableInStations += s.availableSlots();
            ud.numberBikesReservedInStations += s.getReservedBikes();
            ud.numberSlotsReservedInStations += s.getReservedSlots();
        }
        ud.numberStations = stations.size();
        ud.totalNumberBikes = bikes.size();
        ud.totalCapacity = ud.numberBikesAvailableInStations + ud.numberBikesReservedInStations
                + ud.numberSlotsAvailableInStations + ud.numberSlotsReservedInStations;
        ud.numberBikesRent = ud.totalNumberBikes
                - (ud.numberBikesAvailableInStations + ud.numberBikesReservedInStations);

        LocalDateTime current = SimulationDateTime.getCurrentSimulationDateTime();
        return ud;
    }

    public int getNumberStations(){
        return stations.size();
    }
    public List<Station> consultStations() {
        return stations;
    }
    public Station consultStation(int id) {
        List<Station> candidates=stations.stream().filter(station -> station.getId()==id).collect(Collectors.toList());
        if (candidates.isEmpty()) return null;
        if (candidates.size()>1) throw new RuntimeException("more than one station with same id");
        return candidates.get(0);
    }

    public List<Bike> consultBikes() {
        return this.bikes;
    }

}
