/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.core.ManagingEvents;

import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.services.fleetManager.FleetManager;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Bike;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author holger
 */
public class EventManagerTakeBikesFromStation extends EventManaging{

    private Station s;
    
    public EventManagerTakeBikesFromStation(int instant, FleetManager manager, Station s) {
        super(instant, manager);
        this.s=s;
        this.involvedEntities= new ArrayList<>(Arrays.asList(manager, s));
        this.newEntities = null;
        this.oldEntities=null;
    }

    @Override
    public List<EventManaging> execute() throws Exception {
        debugEventLog("At enter the event");
        Bike b=s.removeBikeWithoutReservation();
        if (b==null) {
            setResultInfo(Event.RESULT_TYPE.FAIL, null);
        } else {
            manager.putBikeIntoStore(b);
            involvedEntities.add(b);
            setResultInfo(Event.RESULT_TYPE.SUCCESS, null);
        }
        return null;
    }
}
