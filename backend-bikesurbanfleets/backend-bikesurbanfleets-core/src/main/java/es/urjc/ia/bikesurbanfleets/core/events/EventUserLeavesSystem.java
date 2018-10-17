
package es.urjc.ia.bikesurbanfleets.core.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.urjc.ia.bikesurbanfleets.common.interfaces.Entity;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.users.User;

public class EventUserLeavesSystem extends EventUser {
	private List<Entity> entities;
	
	public EventUserLeavesSystem(int instant, User user) {
			super(instant, user);
				this.entities = new ArrayList<>(Arrays.asList(user));
	}
	
	@Override
	public List<Event> execute() {
		user.leaveSystem();
                user.setState(User.STATE.LEFT_SYSTEM);
		return new ArrayList<>();
	}
	
	@Override
	public List<Entity> getEntities() {
		return entities;
	}

}
