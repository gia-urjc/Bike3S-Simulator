package com.urjc.iagroup.bikesurbanfloats;

public class EventUserWantsToRentBike {
	private User user;
	private Station destination;
	private int time;   //in seconds
	
	public EventUserWantsToRentBike(int instant, User user, Station destination, int time) {
		super(instant);
		this.user = user;
		this.destination = destination;
		this.time = time;
	}

	public User getUser() {
		return user;
	}


	public void setUser(User user) {
		this.user = user;
	}


	public int getTime() {
		return time;
	}


	public void setTime(int time) {
		this.time = time;
	}


	public Station getDestination() {
		return destination;
	}


	public void setDestination(Station destination) {
		this.destination = destination;
	}


	public Event execute() {
		user.setLocation(destination.getLocation());
		if (destination.getBikes()>0)
			return new EventUserRentsBike(getInstant()+time, user, destination);
		else {
			//if there aren't bikes, user decides to go to another station or to leave the system
			decision=user.decides();
			if (decision!=null) {
				int newTime = user.getLocation().distance(decision.getLocation());   //time that user takes in arriving at the new station
				return new EventUserWantsToRentBike(getInstant()+1, user, decision, newTime);
			}
			else
				return null;
		
		}
	}
}
