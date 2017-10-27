package com.urjc.iagroup.bikesurbanfloats.entities.models;

import com.urjc.iagroup.bikesurbanfloats.graphs.GeoPoint;

public interface UserModel<B extends BikeModel, S extends StationModel<B>> {
	
    GeoPoint getPosition();
    
    B getBike();
    S getDestinationStation();
    
    double getWalkingVelocity();
    double getCyclingVelocity();
  
}