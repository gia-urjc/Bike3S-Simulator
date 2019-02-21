package es.urjc.ia.bikesurbanfleets.worldentities.infraestructure;

import es.urjc.ia.bikesurbanfleets.common.demand.DemandManager;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Bike;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Station;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.stream.Collectors;

/**
 * This class contains all the information of all the entities at the system. It
 * provides all the usable methods by the user at the system.
 *
 * @author IAgroup
 */
public class InfrastructureManager {

    /**
     * These are all the stations at the system.
     */
    private List<Station> stations;

    private DemandManager demandManager;

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

    public InfrastructureManager(List<Station> stations, DemandManager demandManager) throws IOException {

        this.demandManager = demandManager;
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
        public double currentGlobalBikeDemand = 0;
        public double currentGlobalSlotDemand = 0;
        public int totalCapacity = 0;
        public double maxdemand = 0;
        public double mindemand = 0;
    }

    public UsageData getCurrentUsagedata() {
        UsageData ud = new UsageData();
        double max = Double.NEGATIVE_INFINITY;
        double min = Double.MAX_VALUE;
        double aux;
        for (Station s : stations) {
            ud.numberBikesAvailableInStations += s.availableBikes();
            ud.numberSlotsAvailableInStations += s.availableSlots();
            ud.numberBikesReservedInStations += s.getReservedBikes();
            ud.numberSlotsReservedInStations += s.getReservedSlots();
            aux = getBikeDemand(s);
            if (aux > max) {
                max = aux;
            }
            if (aux < min) {
                min = aux;
            }
        }
        ud.numberStations = stations.size();
        ud.maxdemand = max;
        ud.mindemand = min;
        ud.totalNumberBikes = bikes.size();
        ud.totalCapacity = ud.numberBikesAvailableInStations + ud.numberBikesReservedInStations
                + ud.numberSlotsAvailableInStations + ud.numberSlotsReservedInStations;
        ud.numberBikesRent = ud.totalNumberBikes
                - (ud.numberBikesAvailableInStations + ud.numberBikesReservedInStations);

        LocalDateTime current = SimulationDateTime.getCurrentSimulationDateTime();
        DemandManager.DemandResult dem = demandManager.getTakeDemandGlobal(current);
        if (dem.hasDemand()) {
            ud.currentGlobalBikeDemand = dem.demand();
        } else {
            System.out.println("[WARNING:] no global bike demand data available at date " + current + ": we assume a demand of 50% of station capacities");
            ud.currentGlobalBikeDemand = ud.totalCapacity / 2D;
        }
        dem = demandManager.getReturnDemandGlobal(current);
        if (dem.hasDemand()) {
            ud.currentGlobalSlotDemand = dem.demand();
        } else {
            System.out.println("[WARNING:] no global bike demand data available at date " + current + ": we assume a demand of 50% of station capacities");
            ud.currentGlobalSlotDemand = ud.totalCapacity / 2D;
        }
        return ud;
    }

    public DemandManager getDemandManager() {
        return demandManager;
    }

    public double getBikeDemand(Station s) {
        DemandManager dm = demandManager;

        LocalDateTime current = SimulationDateTime.getCurrentSimulationDateTime();
        DemandManager.DemandResult takedem = dm.getTakeDemandStation(s.getId(), current);
        if (takedem.hasDemand()) {
            return takedem.demand();
        } else {
            System.out.println("[WARNING:] no bike demand data available for station: " + s.getId() + " at date " + current + ": we assume a demand of bikes of half the capacity");
            return s.getCapacity() / 2D;
        }
    }
    public double POBABILITY_USERSOBEY = 0.9;

    private class PotentialEvent {

        boolean take; //or return
        int settime;
        int expectedendtime;

        PotentialEvent(boolean take, int settime, int expectedendtime) {
            this.take = take;
            this.settime = settime;
            this.expectedendtime = expectedendtime;
        }
    }

    private HashMap<Integer, List<PotentialEvent>> registeredBikeEventsPerStation = new HashMap<>();

    private int getExpectedBikechanges(int stationid, double timeoffset) {
        int changes = 0;
        List<PotentialEvent> list = registeredBikeEventsPerStation.get(stationid);
        if (list == null) {
            return changes;
        }
        long currentinstant = SimulationDateTime.getCurrentSimulationInstant();
        Iterator<PotentialEvent> i = list.iterator();
        while (i.hasNext()) {
            PotentialEvent e = i.next(); // must be called before you can call i.remove()
            if (e.expectedendtime < currentinstant) {
                i.remove();
            } else if (e.expectedendtime<=currentinstant+timeoffset){
                if (e.take) {
                    changes--;
                } else {
                    changes++;
                }
            } //if e.expectedendtime>currentinstant+timeoffset does not count
        }
        return changes;
    }
    public void addExpectedBikechange(int stationid, int endtime, boolean take) {
        int changes = 0;
        List<PotentialEvent> list = registeredBikeEventsPerStation.get(stationid);
        if (list == null) {
            list=new ArrayList<>();
            registeredBikeEventsPerStation.put(stationid, list);
        }
        list.add(new PotentialEvent(take,(int)SimulationDateTime.getCurrentSimulationInstant(),endtime));
     }


 public double getAvailableBikeProbability(Station s, double timeoffset) {

        int estimatedbikes = (int)Math.floor(s.availableBikes() + getExpectedBikechanges(s.getId(),timeoffset ) * POBABILITY_USERSOBEY) ;
        double takedemandattimeoffset=(getBikeDemand(s)*timeoffset)/3600D;
        double retdemandatofsettime=(getSlotDemand(s)*timeoffset)/3600D;
        
        //probability that a bike exists 
        double prob =SellamDistribution.calculateCDFSkellamProbability(retdemandatofsettime, takedemandattimeoffset, estimatedbikes);
        
        return prob;
}
public double getAvailableSlotProbability(Station s, double timeoffset) {

        int estimatedslots = (int)Math.floor(s.availableSlots() + getExpectedBikechanges(s.getId(),timeoffset ) * POBABILITY_USERSOBEY) ;
        double takedemandattimeoffset=(getBikeDemand(s)*timeoffset)/3600D;
        double retdemandatofsettime=(getSlotDemand(s)*timeoffset)/3600D;
        
        //probability that a bike exists 
        double prob =SellamDistribution.calculateCDFSkellamProbability(takedemandattimeoffset, retdemandatofsettime, estimatedslots);
        
        return prob;
}

    public double getSlotDemand(Station s) {
        DemandManager dm = demandManager;
        LocalDateTime current = SimulationDateTime.getCurrentSimulationDateTime();
        DemandManager.DemandResult retdem = dm.getReturnDemandStation(s.getId(), current);
        if (retdem.hasDemand()) {
            return (retdem.demand());
        } else {
            System.out.println("[WARNING:] no slot demand data available for station: " + s.getId() + " at date " + current + ": we assume a demand of slots of half the capacity");
            return s.getCapacity() / 2D;
        }
    }

    public List<Station> consultStations() {
        return stations;
    }

    public List<Bike> consultBikes() {
        return this.bikes;
    }

}
