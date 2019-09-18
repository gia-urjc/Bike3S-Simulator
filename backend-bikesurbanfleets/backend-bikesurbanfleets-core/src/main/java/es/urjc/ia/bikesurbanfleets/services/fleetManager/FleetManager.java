/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.services.fleetManager;

import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Station;
import java.util.List;
import java.util.PriorityQueue;

/**
 *
 * @author holger
 */
public abstract class FleetManager {

    /**
     * These are all the stations at the system.
     */
    private List<Station> stations;

    public FleetManager(SimulationServices simulationServices) {
        stations = simulationServices.getInfrastructureManager().consultStations();
    }
    //method that can check the stations and does corrective actions
    public abstract void initialActions(PriorityQueue<Event> restEvents);
        
    //method that can check the stations and does corrective actions
    public abstract void doManagementActions(PriorityQueue<Event> restEvents);
    
}
