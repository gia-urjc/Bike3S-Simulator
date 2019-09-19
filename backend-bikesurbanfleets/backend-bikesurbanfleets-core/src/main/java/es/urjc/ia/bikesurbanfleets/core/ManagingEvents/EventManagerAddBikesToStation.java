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

/**
 *
 * @author holger
 */
public class EventManagerAddBikesToStation extends EventManaging{

    private Station s;
    private int numerbikes;
    
    public EventManagerAddBikesToStation(int instant, FleetManager manager, Station s, int numberbikes) {
        super(instant, manager);
        this.s=s;
        this.numerbikes=numberbikes;
        this.involvedEntities= new ArrayList<>(Arrays.asList(manager, s));
        this.newEntities = null;
        this.oldEntities=null;
    }

    @Override
    public Event execute() throws Exception {
        debugEventLog("At enter the event");
        Bike b=manager.getBike();
        if (b==null) setResult(Event.RESULT_TYPE.FAIL);
        boolean sucess=s.returnBikeWithoutReservation(b);
        if (sucess) setResult(Event.RESULT_TYPE.SUCCESS);
        else setResult(Event.RESULT_TYPE.FAIL);   

        return null;
    }
    
}
