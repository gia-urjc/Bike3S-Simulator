/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.services.fleetManager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.core.ManagingEvents.EventManagerAddBikesToStation;
import es.urjc.ia.bikesurbanfleets.core.ManagingEvents.EventManagerTakeBikesFromStation;
import es.urjc.ia.bikesurbanfleets.core.ManagingEvents.EventManaging;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author holger
 */
@FleetManagerType("FileBasedFleetManager")
public class FileBasedFleetManager extends FleetManager{

    public class FleetManagerParameters {
        String EventFile=null;
    }
    FleetManagerParameters parameters=null;

    public FileBasedFleetManager(JsonObject parameterdef, SimulationServices ss) throws Exception {
        super(ss);
        this.parameters = new FleetManagerParameters();
        getParameters(parameterdef, this.parameters);
     }

    //this manager does nothing
    public List<EventManaging> checkSituation(){return null;}

    @Override
    public List<EventManaging> initialActions() {
        try {
            return generateEventsFromFile();
        } catch (FileNotFoundException ex) {
            throw new RuntimeException("error in managing events");
        }
        
    }
    
    private List<EventManaging> generateEventsFromFile() throws FileNotFoundException {
        Gson gson= new Gson();

        FileReader reader = new FileReader(parameters.EventFile);
        managingEvents events = gson.fromJson(reader, managingEvents.class);
        List<EventManaging> newEvents= new LinkedList<EventManaging>();
        for(managingEvent ev:events.managingEvents){
            processEventEntry(ev, newEvents);
        }
        return newEvents;
    }

    private void processEventEntry(managingEvent evdescription, List<EventManaging> newevs){
        //first check that everything is ok
        if (evdescription.timeInstant<0 || evdescription.travelTime<=0) {
            throw new RuntimeException("invalid management event");
        }
        Station getstation= stationManager.consultStation(evdescription.idInitialStation);
        if (getstation==null) throw new RuntimeException("take station does not exist");
        Station retstation= stationManager.consultStation(evdescription.idEndStation);
        if (retstation==null) throw new RuntimeException("return station does not exist");
        
        newevs.add(new EventManagerTakeBikesFromStation(evdescription.timeInstant, this, getstation));
        newevs.add(new EventManagerAddBikesToStation(evdescription.timeInstant+evdescription.travelTime, this, retstation));
    }
    
    private class managingEvents{
        List<managingEvent> managingEvents;
    } 
    private class managingEvent{
        int timeInstant;
        int idInitialStation;
        int idEndStation;
        int travelTime;
    } 
}
