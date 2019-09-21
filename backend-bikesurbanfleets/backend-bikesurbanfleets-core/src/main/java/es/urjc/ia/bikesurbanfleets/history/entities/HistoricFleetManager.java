/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.history.entities;

import com.google.gson.annotations.Expose;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.history.JsonIdentifier;
import es.urjc.ia.bikesurbanfleets.services.fleetManager.FleetManager;
import es.urjc.ia.bikesurbanfleets.services.fleetManager.FleetManagerType;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;

/**
 *
 * @author holger
 */
@JsonIdentifier("fleetmanager")
public class HistoricFleetManager implements HistoricEntity{

    @Expose
    private int id;
    
    @Expose
    private String name;
 

    public HistoricFleetManager(FleetManager m) {
        this.id = m.getId();
        this.name=m.getClass().getAnnotation(FleetManagerType.class).value();
    }
    @Override
    public int getId() {
        return id;
    }
    
}
