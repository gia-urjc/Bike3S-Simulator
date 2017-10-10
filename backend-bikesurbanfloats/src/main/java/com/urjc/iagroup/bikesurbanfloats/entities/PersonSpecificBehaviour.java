package com.urjc.iagroup.bikesurbanfloats.entities;

import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;

public interface PersonSpecificBehaviour extends PersonCommonBehaviour {
   
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