/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.worldentities.users;

import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;

/**
 *
 * @author holger
 */
public class UserDecisionStation implements UserDecision{
    
    public final Station station;
    public final boolean reserve;
    
    public UserDecisionStation(Station station, boolean reserve){
        this.station=station;
        this.reserve=reserve;
    }
    
}
