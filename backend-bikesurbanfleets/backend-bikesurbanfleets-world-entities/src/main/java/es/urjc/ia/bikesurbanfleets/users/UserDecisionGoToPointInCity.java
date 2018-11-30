/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.users;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;

/**
 *
 * @author holger
 */
public class UserDecisionGoToPointInCity implements UserDecision{
    public final GeoPoint point;
    
    public UserDecisionGoToPointInCity(GeoPoint point){
        this.point=point;
    }

}
