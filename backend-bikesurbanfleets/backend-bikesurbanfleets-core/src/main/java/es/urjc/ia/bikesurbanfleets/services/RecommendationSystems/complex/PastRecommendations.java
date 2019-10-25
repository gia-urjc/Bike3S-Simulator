/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex;

import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author holger
 */
public class PastRecommendations {
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //the following methods store information on the receommendations done before
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    private static class PotentialEvent {

        boolean take; //or return
        int expectedendtime;
        
        PotentialEvent(boolean take, int expectedendtime) {
            this.take = take;
            this.expectedendtime = expectedendtime;
        }
    }

    //the list will be ordered by the expectedendtime
    private HashMap<Integer, LinkedList<PotentialEvent>> registeredBikeEventsPerStation = new HashMap<>();

    //result class for getExpectedBikechanges
    public static class ExpBikeChangeResult {

        public int changes = 0;
        public int minpostchanges = 0;
        public int maxpostchanges = 0;
        public long lastendinstantexpected=0;
    }

    public ExpBikeChangeResult getExpectedBikechanges(int stationid, double timeoffset) {
        ExpBikeChangeResult er = new ExpBikeChangeResult();
        long currentinstant = SimulationDateTime.getCurrentSimulationInstant();
        er.lastendinstantexpected=currentinstant;
        int postchanges = 0;
        List<PotentialEvent> list = registeredBikeEventsPerStation.get(stationid);
        if (list == null) {
            return er;
        }
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
                if (e.expectedendtime>er.lastendinstantexpected) er.lastendinstantexpected=e.expectedendtime;
            } else {// e.expectedendtime>currentinstant+timeoffset are taken in to consideration if compromised is true
                if (e.take) {
                    postchanges--;
                } else {
                    postchanges++;
                }
                if (postchanges < er.minpostchanges) {
                    er.minpostchanges = postchanges;
                }
                if (postchanges > er.maxpostchanges) {
                    er.maxpostchanges = postchanges;
                }
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
        int endtime = (int) SimulationDateTime.getCurrentSimulationInstant()+ timeoffset;
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

}
