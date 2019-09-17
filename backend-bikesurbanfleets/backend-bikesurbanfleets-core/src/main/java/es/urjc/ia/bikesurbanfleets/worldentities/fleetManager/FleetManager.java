/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.worldentities.fleetManager;

import es.urjc.ia.bikesurbanfleets.core.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Station;
import java.util.List;

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
    public abstract void doManagementActions();
    
}
