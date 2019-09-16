/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.worldentities.fleetManager;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.core.services.SimulationServices;

/**
 *
 * @author holger
 */
@FleetManagementSystemType("None")
public class DummyFleetManager extends FleetManager{
 
    @FleetManagementSystemParameters
    public class FleetManagerParameters {
    }

    public DummyFleetManager(JsonObject recomenderdef, SimulationServices ss) {
        super(ss);
    }

    //this manager does nothing
    public void doManagementActions(){return;};

}
