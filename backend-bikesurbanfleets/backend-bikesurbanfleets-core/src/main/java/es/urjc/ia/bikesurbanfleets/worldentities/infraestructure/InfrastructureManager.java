package es.urjc.ia.bikesurbanfleets.worldentities.infraestructure;

import es.urjc.ia.bikesurbanfleets.common.demand.DemandManager;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Bike;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Station;

import java.io.IOException;
import java.time.LocalDateTime;
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
        public int numberStations=0;
        public int totalNumberBikes = 0;
        public int numberBikesRent = 0;
        public int numberBikesAvailableInStations = 0;
        public int numberSlotsAvailableInStations = 0;
        public int numberBikesReservedInStations = 0;
        public int numberSlotsReservedInStations = 0;
        public double currentGlobalBikeDemand = 0;
        public double currentGlobalSlotDemand = 0;
        public int totalCapacity = 0;
        public double maxdemand=0;
        public double mindemand=0;
    }

    public UsageData getCurrentUsagedata() {
        UsageData ud = new UsageData();
        double max=Double.NEGATIVE_INFINITY;
        double min=Double.MAX_VALUE;
        double aux;
        for (Station s : stations) {
            ud.numberBikesAvailableInStations += s.availableBikes();
            ud.numberSlotsAvailableInStations += s.availableSlots();
            ud.numberBikesReservedInStations += s.getReservedBikes();
            ud.numberSlotsReservedInStations += s.getReservedSlots();
            aux=getBikeDemand(s);
            if (aux>max) max=aux;
            if (aux<min) min =aux;
        }
        ud.numberStations=stations.size();
        ud.maxdemand=max;
        ud.mindemand=min;
        ud.totalNumberBikes = bikes.size();
        ud.totalCapacity = ud.numberBikesAvailableInStations + ud.numberBikesReservedInStations
                + ud.numberSlotsAvailableInStations + ud.numberSlotsReservedInStations;
        ud.numberBikesRent = ud.totalNumberBikes
                - (ud.numberBikesAvailableInStations + ud.numberBikesReservedInStations);
        int currentGlobalBikeDemand = 0;
        int currentGlobalSlotDemand = 0;

        LocalDateTime current = SimulationDateTime.getCurrentSimulationDateTime();
        DemandManager.DemandResult dem = demandManager.getTakeDemandGlobal(DemandManager.Month.toDemandMangerMonth(current.getMonth()),
                DemandManager.Day.toDemandMangerDay(current.getDayOfWeek()), current.getHour());
        if (dem.hasDemand()) {
            ud.currentGlobalBikeDemand = dem.demand();
        } else {
            System.out.println("[WARNING:] no global bike demand data available at date " + current + ": we assume a demand of 50% of station capacities");
            ud.currentGlobalBikeDemand = ud.totalCapacity / 2D;
        }
        dem = demandManager.getReturnDemandGlobal(DemandManager.Month.toDemandMangerMonth(current.getMonth()),
                DemandManager.Day.toDemandMangerDay(current.getDayOfWeek()), current.getHour());
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
        DemandManager.DemandResult takedem = dm.getTakeDemandStation(s.getId(), DemandManager.Month.toDemandMangerMonth(current.getMonth()),
                DemandManager.Day.toDemandMangerDay(current.getDayOfWeek()), current.getHour());
        if (takedem.hasDemand()) {
            return takedem.demand();
        } else {
            System.out.println("[WARNING:] no bike demand data available for station: " + s.getId() + " at date " + current + ": we assume a demand of bikes of half the capacity");
            return s.getCapacity() / 2D;
        }
    }

    public double getSlotDemand(Station s) {
        DemandManager dm = demandManager;
        LocalDateTime current = SimulationDateTime.getCurrentSimulationDateTime();
        DemandManager.DemandResult retdem = dm.getReturnDemandStation(s.getId(), DemandManager.Month.toDemandMangerMonth(current.getMonth()),
                DemandManager.Day.toDemandMangerDay(current.getDayOfWeek()), current.getHour());
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
