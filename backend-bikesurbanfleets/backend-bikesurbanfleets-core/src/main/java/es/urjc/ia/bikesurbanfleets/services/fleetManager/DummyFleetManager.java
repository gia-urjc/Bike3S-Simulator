/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.services.fleetManager;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import java.util.PriorityQueue;

/**
 *
 * @author holger
 */
@FleetManagerType("Dummy")
public class DummyFleetManager extends FleetManager{
 
    public class FleetManagerParameters {
    }
    FleetManagerParameters parameters=null;

    public DummyFleetManager(JsonObject parameterdef, SimulationServices ss) throws Exception {
        super(ss);
        this.parameters = new FleetManagerParameters();
        getParameters(parameterdef, this.parameters);
     }

    //this manager does nothing
    public void doManagementActions(PriorityQueue<Event> restEvents){return;};

    public void initialActions(PriorityQueue<Event> restEvents) {return;}
}
