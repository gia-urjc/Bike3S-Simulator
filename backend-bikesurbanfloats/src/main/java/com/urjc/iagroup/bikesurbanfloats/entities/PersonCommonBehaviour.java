package com.urjc.iagroup.bikesurbanfloats.entities;

import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;

public interface PersonCommonBehaviour {

	public int getId();
	
    public GeoPoint getPosition();
    public void setPosition(GeoPoint position);
    public void setPosition(Double latitude, Double longitude);
    
    public Bike getBike();
    public boolean hasBike();
    public boolean hasReservedBike();
    public boolean hasReservedSlot();
    public void reservesBike(Station station);
    public void reservesSlot(Station station);
    public void cancelsBikeReservation(Station station);
    public void cancelsSlotReservation(Station station);
    
    public boolean removeBikeFrom(Station station);
    public boolean returnBikeTo(Station station);
    
    public Station getDestinationStation();
    public void setDestinationStation(Station destinationStation);
    
    public Double getAverageVelocity();
    public int timeToReach(GeoPoint destination);
    
    public String toString();
   
}
