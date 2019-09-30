/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.services.fleetManager;

import es.urjc.ia.bikesurbanfleets.common.interfaces.Entity;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.core.ManagingEvents.EventManaging;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.StationManager;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Bike;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

/**
 *
 * @author holger
 */
public abstract class FleetManager implements Entity{

    /**
     * These are all the stations at the system.
     */
    StationManager stationManager;
    //the store of bikes that are taken from the system
    private List<Bike> bikestore;

    public FleetManager(SimulationServices simulationServices) {
        stationManager = simulationServices.getStationManager();
        bikestore=new LinkedList<Bike>();
    }
    //method that can check the stations and does corrective actions
    public abstract List<EventManaging> initialActions();
        
    //method that can check the stations and does corrective actions
    public abstract List<EventManaging> checkSituation();
    
    //method for getting a bike from a station
    //returns true if sucessful and false otherwise
    final public void putBikeIntoStore(Bike b) {
        bikestore.add(b);
    }
    //returns true if sucessful and false otherwise
    public final Bike getBikeFromStore(){
        if (bikestore.size()==0) return null;
        return bikestore.remove(0);
    }

    final public int getId(){
        return 1;
    }

    final public String toString(){
        return this.getClass().getAnnotation(FleetManagerType.class).value();
    };

}
