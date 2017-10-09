package com.urjc.iagroup.bikesurbanfloats.entities;

import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;

public interface PersonBehaviour {
	
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
    
	public boolean decidesToLeaveSystem();

 public Station determineStation();
    
 // it musts call reservesBike method inside it 
 public boolean decidesToReserveBike(Station station);

 // it musts call reservesSlot method inside it 
 public boolean decidesToReserveSlot(Station station);

	// returns: user decides where to go to to ride his bike (not to a station)
 public GeoPoint decidesNextPoint();
    	
 // returns: true -> user goes to a station; false -> user rides his bike to a site which isn't a station
 public boolean decidesToReturnBike();
     
 // walked distance during a time period 
 public void updatePosition(int time);
    	
 public boolean decidesToRentBikeAtOtherStation(); 

}