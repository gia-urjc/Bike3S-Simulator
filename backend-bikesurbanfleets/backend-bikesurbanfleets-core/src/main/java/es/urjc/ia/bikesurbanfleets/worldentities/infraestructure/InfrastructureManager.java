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
public class InfrastructureManager {

    /**
     * These are all the stations at the system.
     */
    private List<Station> stations;

    private DemandManager demandManager;

    private boolean demandmissingwarning = false;
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
        public double maxBikedemand = 0;
        public double minBikedemand = 0;
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
            aux = getCurrentBikeDemand(s);
            if (aux > max) {
                max = aux;
            }
            if (aux < min) {
                min = aux;
            }
        }
        ud.numberStations = stations.size();
        ud.maxBikedemand = max;
        ud.minBikedemand = min;
        ud.totalNumberBikes = bikes.size();
        ud.totalCapacity = ud.numberBikesAvailableInStations + ud.numberBikesReservedInStations
                + ud.numberSlotsAvailableInStations + ud.numberSlotsReservedInStations;
        ud.numberBikesRent = ud.totalNumberBikes
                - (ud.numberBikesAvailableInStations + ud.numberBikesReservedInStations);

        LocalDateTime current = SimulationDateTime.getCurrentSimulationDateTime();
        ud.currentGlobalBikeDemand = demandManager.getTakeDemandGlobal(current);
        ud.currentGlobalSlotDemand = demandManager.getReturnDemandGlobal(current);
        return ud;
    }

    public double POBABILITY_USERSOBEY = 0.9;

    private class PotentialEvent {

        boolean take; //or return
        int expectedendtime;

        PotentialEvent(boolean take, int expectedendtime) {
            this.take = take;
            this.expectedendtime = expectedendtime;
        }
    }

    //the list will be ordered by the expectedendtime
    private HashMap<Integer, LinkedList<PotentialEvent>> registeredBikeEventsPerStation = new HashMap<>();

    //global variables used for getExpectedBikechanges
    public class ExpBikeChangeResult{
        public int changes = 0;
        public int minpostchanges=0;
        public int maxpostchanges=0;
    }  
    public ExpBikeChangeResult getExpectedBikechanges(int stationid, double timeoffset) {
        ExpBikeChangeResult er=new ExpBikeChangeResult();
         int postchanges=0;
        List<PotentialEvent> list = registeredBikeEventsPerStation.get(stationid);
        if (list == null) {
            return er;
        }
        long currentinstant = SimulationDateTime.getCurrentSimulationInstant();
        Iterator<PotentialEvent> i = list.iterator();
        while (i.hasNext()) {
            PotentialEvent e = i.next(); // must be called before you can call i.remove()
            if (e.expectedendtime < currentinstant) {
                i.remove();
            } else if (e.expectedendtime < currentinstant + timeoffset) {
                if (e.take) {
                    er.changes--;
                } else {
                    er.changes++;
                }
            } else {// e.expectedendtime>currentinstant+timeoffset are taken in to consideration if compromised is true
                if (e.take) {
                    postchanges--;
                } else {
                    postchanges++;
                }
                if (postchanges<er.minpostchanges) er.minpostchanges=postchanges;
                if (postchanges>er.maxpostchanges) er.maxpostchanges=postchanges;
            }
        }
        return er;
    }

    public void addExpectedBikechange(int stationid, int timeoffset, boolean take) {
        int changes = 0;
        LinkedList<PotentialEvent> list = registeredBikeEventsPerStation.get(stationid);
        if (list == null) {
            list = new LinkedList<>();
            registeredBikeEventsPerStation.put(stationid, list);
        }
        int endtime = (int) SimulationDateTime.getCurrentSimulationInstant() + timeoffset;
        //put the element in its position in the list
        boolean done = false;
        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.get(i).expectedendtime <= endtime) {
                list.add(i + 1, new PotentialEvent(take, endtime));
                done = true;
                break;
            }
        }
        if (!done) {
            list.add(0, new PotentialEvent(take, endtime));
        }
    }

    public double getAvailableBikeProbability(Station s, double timeoffset, boolean takeintoaccountexpected, boolean takeintoaccountcompromised) {
        int estimatedbikes = s.availableBikes();
        if (takeintoaccountexpected) {
            ExpBikeChangeResult er=getExpectedBikechanges(s.getId(), timeoffset); 
            estimatedbikes+= (int) Math.floor(er.changes* POBABILITY_USERSOBEY);
            if (takeintoaccountcompromised) {
    //            if ((estimatedbikes+minpostchanges)<=0){
                    estimatedbikes+= (int) Math.floor(er.minpostchanges* POBABILITY_USERSOBEY);
    //            }
            }
        }
        double takedemandattimeoffset = (getCurrentBikeDemand(s) * timeoffset) / 3600D;
        double retdemandatofsettime = (getCurrentSlotDemand(s) * timeoffset) / 3600D;
        //probability that a bike exists 
        int k = 1 - estimatedbikes;
        double prob = SellamDistribution.calculateCDFSkellamProbability(retdemandatofsettime, takedemandattimeoffset, k);

        return prob;
    }

    public double getAvailableSlotProbability(Station s, double timeoffset, boolean takeintoaccountexpected, boolean takeintoaccountcompromised) {
        int estimatedslots = s.availableSlots();
        if (takeintoaccountexpected) {
            ExpBikeChangeResult er=getExpectedBikechanges(s.getId(), timeoffset); 
            estimatedslots-= (int) Math.floor(er.changes* POBABILITY_USERSOBEY);
            if (takeintoaccountcompromised) {
     //           if ((estimatedslots-maxpostchanges)<=0){
                    estimatedslots-= (int) Math.floor(er.maxpostchanges* POBABILITY_USERSOBEY);
     //           }
            }
        }
        double takedemandattimeoffset = (getCurrentBikeDemand(s) * timeoffset) / 3600D;
        double retdemandatofsettime = (getCurrentSlotDemand(s) * timeoffset) / 3600D;

        //probability that a bike exists 
        int k = 1 - estimatedslots;
        double prob = SellamDistribution.calculateCDFSkellamProbability(takedemandattimeoffset, retdemandatofsettime, k);

        return prob;
    }

    public double getCurrentSlotDemand(Station s) {
        LocalDateTime current = SimulationDateTime.getCurrentSimulationDateTime();
        return demandManager.getReturnDemandStation(s.getId(), current);
    }

    public double getCurrentBikeDemand(Station s) {
        LocalDateTime current = SimulationDateTime.getCurrentSimulationDateTime();
        return demandManager.getTakeDemandStation(s.getId(), current);
    }
    public double getFutureSlotDemand(Station s, int secondsoffset) {
        LocalDateTime current = SimulationDateTime.getCurrentSimulationDateTime().plusSeconds(secondsoffset);
        return demandManager.getReturnDemandStation(s.getId(), current);
    }

    public double getFutureBikeDemand(Station s, int secondsoffset) {
        LocalDateTime current = SimulationDateTime.getCurrentSimulationDateTime().plusSeconds(secondsoffset);
        return demandManager.getTakeDemandStation(s.getId(), current);
    }
    public double getFutureGlobalSlotDemand(int secondsoffset) {
        LocalDateTime current = SimulationDateTime.getCurrentSimulationDateTime().plusSeconds(secondsoffset);
        return demandManager.getReturnDemandGlobal( current);
    }

    public double getFutureGlobalBikeDemand(int secondsoffset) {
        LocalDateTime current = SimulationDateTime.getCurrentSimulationDateTime().plusSeconds(secondsoffset);
        return demandManager.getTakeDemandGlobal(current);
    }

    public double getCurrentFutueScaledSlotDemandNextHour(Station s) {
        LocalDateTime current = SimulationDateTime.getCurrentSimulationDateTime();
        LocalDateTime futuredate = current.plusHours(1);
        double currendem = demandManager.getReturnDemandStation(s.getId(), current);
        double futuredem = demandManager.getReturnDemandStation(s.getId(), futuredate);
        double futureprop = ((double) current.getMinute()) / 59D;
        return futuredem * futureprop + (1 - futureprop) * currendem;
    }

    public double getCurrentFutueScaledBikeDemandNextHour(Station s) {
        LocalDateTime current = SimulationDateTime.getCurrentSimulationDateTime();
        LocalDateTime futuredate = current.plusHours(1);
        double currendem = demandManager.getTakeDemandStation(s.getId(), current);
        double futuredem = demandManager.getTakeDemandStation(s.getId(), futuredate);
        double futureprop = ((double) current.getMinute()) / 59D;
        return futuredem * futureprop + (1 - futureprop) * currendem;
    }

    public List<Station> consultStations() {
        return stations;
    }

    public List<Bike> consultBikes() {
        return this.bikes;
    }

}
