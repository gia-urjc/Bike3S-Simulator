package com.urjc.iagroup.bikesurbanfloats.entities.models;

import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;

public interface UserModel<BikeType extends BikeModel, StationType extends StationModel> {
	
    GeoPoint getPosition();
    
    BikeType getBike();
    boolean hasBike();
    boolean hasReservedBike();
    boolean hasReservedSlot();
    
    StationType getDestinationStation();
    
    double getAverageVelocity();
   
}
