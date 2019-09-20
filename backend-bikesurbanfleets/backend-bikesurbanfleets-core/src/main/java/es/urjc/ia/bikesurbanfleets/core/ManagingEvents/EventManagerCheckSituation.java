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
public class EventManagerCheckSituation extends EventManaging{
    
    public EventManagerCheckSituation(int instant, FleetManager manager) {
        super(instant, manager);
        this.involvedEntities= new ArrayList<>(Arrays.asList(manager));
        this.newEntities = null;
        this.oldEntities=null;
    }

    @Override
    public List<EventManaging> execute() throws Exception {
        manager.checkSituation();
        return null;
    }
  
}
