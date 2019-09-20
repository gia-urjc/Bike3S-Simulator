/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.core.ManagingEvents;

import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.services.fleetManager.FleetManager;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Bike;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Station;
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
            setResult(Event.RESULT_TYPE.FAIL);
        } else {
            manager.putBikeIntoStore(b);
            involvedEntities.add(b);
            setResult(Event.RESULT_TYPE.SUCCESS);
        }
         return null;
    }
}
