package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.config.SimulationConfiguration;
import com.urjc.iagroup.bikesurbanfloats.entities.User;

import java.util.List;

public class EventUserAppears extends EventUser {

    public EventUserAppears(int instant, User user, SimulationConfiguration simulationConfiguration) {
        super(instant, user, simulationConfiguration);
    }
    
    public List<Event> execute() {
        return manageBikeReservationDecision();
    }
}