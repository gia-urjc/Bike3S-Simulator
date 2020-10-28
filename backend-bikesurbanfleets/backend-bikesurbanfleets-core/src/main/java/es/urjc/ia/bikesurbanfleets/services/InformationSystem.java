package es.urjc.ia.bikesurbanfleets.services;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.services.graphManager.GraphManager;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.StationManager;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.ArrayList;

/**
 * @author IAgroup
 *
 */
public class InformationSystem {

    private StationManager stationManager;
    private GraphManager graphManager;

    private class stationInfo {

        Station station;
        double distance;
    }

    public InformationSystem(StationManager infraestructureManager, GraphManager graphManager) {
        this.stationManager = infraestructureManager;
        this.graphManager = graphManager;
    }

    public List<Recommendation> getStationsOrderedByWalkDistanceWithinMaxDistance(GeoPoint point, double maxdist) {
        List<stationInfo> candidates = getStationsWithinMaxDistance(point, maxdist);
        return candidates.stream().sorted(byWalkDistance()).
                map(sq -> new Recommendation(sq.station, sq.distance, 0, 0, null))
                .collect(Collectors.toList());
    }

    public List<Recommendation> getAllStationsOrderedByWalkDistance(GeoPoint point) {
        List<stationInfo> candidates = getAllStations(point);
        return candidates.stream().sorted(byWalkDistance()).
                map(sq -> new Recommendation(sq.station, sq.distance, 0, 0, null))
                .collect(Collectors.toList());
    }

    public List<Recommendation> getStationsOrderedByWalkDistanceWithinMaxDistanceWithBikes(GeoPoint point, double maxdist) {
        List<stationInfo> candidates = getStationsWithinMaxDistance(point, maxdist);
        return candidates.stream().
                filter(candidate -> candidate.station.availableBikes() > 0).
                sorted(byWalkDistance()).
                map(sq -> new Recommendation(sq.station, sq.distance, 0, 0, null))
                .collect(Collectors.toList());
    }

    public List<Recommendation> getAllStationsOrderedByWalkDistanceWithSlots(GeoPoint point) {
        List<stationInfo> candidates = getAllStations(point);
        return candidates.stream().
                filter(candidate -> candidate.station.availableSlots() > 0).
                sorted(byWalkDistance()).
                map(sq -> new Recommendation(sq.station, sq.distance, 0, 0, null))
                .collect(Collectors.toList());
    }

    public List<Station> getAllStations() {
        return stationManager.consultStations();
    }

    // method for getting all stations within a certain walking distance
    // precalculates walk distance
    // ALL stations within the maxdistance are returned
    private List<stationInfo> getStationsWithinMaxDistance(GeoPoint position, double maxdist) {
        List<stationInfo> candidates = new ArrayList<>();
        for (Station s : stationManager.consultStations()) {
            double dist = graphManager.estimateDistance(position, s.getPosition(), "foot");
            if (dist <= maxdist) {
                stationInfo scd = new stationInfo();
                scd.station = s;
                scd.distance = dist;
                candidates.add(scd);
            }
        }
        return candidates;
    }

    // method for getting all stations 
    // precalculates walk distance
    // ALL stations are returned
    private List<stationInfo> getAllStations(GeoPoint position) {
        List<stationInfo> candidates = new ArrayList<>();
        for (Station s : stationManager.consultStations()) {
            double dist = graphManager.estimateDistance(position, s.getPosition(), "foot");
            stationInfo scd = new stationInfo();
            scd.station = s;
            scd.distance = dist;
            candidates.add(scd);
        }
        return candidates;
    }

    private static Comparator<stationInfo> byWalkDistance() {
        return (s1, s2) -> Double.compare(
                s1.distance, s2.distance);
    }

    public void printRecomendations(List<Recommendation> su, double maxdist, boolean rentbike, int maxnumber) {
        long maxn = Math.min(maxnumber, su.size());
        System.out.println();
        if (rentbike) {
            System.out.println("Time (take):" + SimulationDateTime.getCurrentSimulationDateTime() + "(" + SimulationDateTime.getCurrentSimulationInstant() + ")");
        } else {
            System.out.println("Time (return):" + SimulationDateTime.getCurrentSimulationDateTime() + "(" + SimulationDateTime.getCurrentSimulationInstant() + ")");
        }
        if (su.size() < 1) {
            if (rentbike) {
                System.out.println("[Warn] No recommendations found for renting at " + maxdist + "meters.");
            } else {
                System.out.println("[Warn] No recommendations found for returning.");
            }
        } else {
            int i = 1;
            System.out.println("             id av ca    wdist");
            for (Recommendation r : su) {
                if (i > maxnumber) {
                    break;
                }
                System.out.format("%-3d Station %3d %2d %2d %8.2f %n",
                        i,
                        r.getStation().getId(),
                        r.getStation().availableBikes(),
                        r.getStation().getCapacity(),
                        r.getWalkdistance());
                i++;
            }
        }
    }
}
