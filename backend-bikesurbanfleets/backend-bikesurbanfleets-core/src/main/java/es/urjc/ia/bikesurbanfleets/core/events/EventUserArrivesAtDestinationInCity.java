package es.urjc.ia.bikesurbanfleets.core.events;

import java.util.ArrayList;
import java.util.List;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Entity;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.users.User;

public class EventUserArrivesAtDestinationInCity extends EventUser {
	private List<Entity> entities;
	
	public EventUserArrivesAtDestinationInCity(int instant, User user) {
		super(instant, user);
		entities = new ArrayList();
	}
	
	@Override
	public List<Event> execute() {
		debugEventLog("User arrives at his destination in city");
		user.setInstant(this.instant);
		user.setPosition(user.getDestinationPlace());
		user.leaveSystem();
  debugClose(user, user.getId());
  return new ArrayList<>();
	}
	
	@Override
	public List<Entity> getEntities() {
		return entities;
	}

}
