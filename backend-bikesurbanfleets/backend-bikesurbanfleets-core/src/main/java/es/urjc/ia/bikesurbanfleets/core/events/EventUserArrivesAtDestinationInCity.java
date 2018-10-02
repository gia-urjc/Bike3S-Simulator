package es.urjc.ia.bikesurbanfleets.core.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoRoute;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Entity;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.users.User;

public class EventUserArrivesAtDestinationInCity extends EventUser {
	private List<Entity> entities;
	
	public EventUserArrivesAtDestinationInCity(int instant, User user) {
		super(instant, user);
		entities = new ArrayList<>(Arrays.asList(user));
	}
	
	@Override
	public List<Event> execute() {
		debugEventLog("User arrives at his destination in city");
		System.out.println("ult ruta: "+user.getRoute());
		user.setInstant(this.instant);
		GeoPoint lastPosition = user.getPosition();
 	user.setPosition(user.getDestinationPlace());
 	
 	//testing code
 	GeoRoute lastRoute = user.getRoute();
		user.leaveSystem();
		GeoRoute newRoute = user.getRoute();
		GeoPoint newPosition = user.getPosition();
		if (newRoute == null && lastRoute != null)
		 	System.out.println("ruta ha cambiado: si");
		else 
		 	System.out.println("ruta ha cambiado: "+lastRoute.equals(newRoute));
		if (lastPosition != null && newPosition == null) 
		 	System.out.println("posicion ha cambiado: si");
		else
		 	System.out.println("ruta ha cambiado: "+lastPosition.equals(newPosition));
		
		debugClose(user, user.getId());
		return new ArrayList<>();
	}
	
	@Override
	public List<Entity> getEntities() {
		return entities;
	}

}
