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
public class EventManagerAddBikesToStation extends EventManaging{

    private Station s;
    
    public EventManagerAddBikesToStation(int instant, FleetManager manager, Station s) {
        super(instant, manager);
        this.s=s;
        this.involvedEntities= new ArrayList<>(Arrays.asList(manager, s));
        this.newEntities = null;
        this.oldEntities=null;
    }

    public List<EventManaging> execute() throws Exception {
        debugEventLog("At enter the event");
        Bike b = manager.getBikeFromStore();
        if (b==null) throw new RuntimeException("No bikes in store");
        boolean sucess=s.returnBikeWithoutReservation(b);
        involvedEntities.add(b);

        if (sucess) {
            setResult(Event.RESULT_TYPE.SUCCESS);
        }
        else { //bike can not put into slot 
            //TODO: what to do here
            manager.putBikeIntoStore(b);
            setResult(Event.RESULT_TYPE.FAIL);
        }
        return null;
    }
    
}
