package es.urjc.ia.bikesurbanfleets.core.events;

import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Entity;
import es.urjc.ia.bikesurbanfleets.entities.Station;
import es.urjc.ia.bikesurbanfleets.entities.User;
import es.urjc.ia.bikesurbanfleets.users.UserMemory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventUserArrivesAtStationToRentBikeWithoutReservation extends EventUser {

    private List<Entity> entities;
    private Station station;
    
    public EventUserArrivesAtStationToRentBikeWithoutReservation(int instant, User user, Station station) {
        super(instant, user);
        this.entities = new ArrayList<>(Arrays.asList(user, station));
        this.station = station;
    }

    public Station getStation() {
        return station;
    }

    @Override
    public List<Event> execute() throws Exception {
        List<Event> newEvents = new ArrayList<>();
        user.setPosition(station.getPosition());
        
        if (user.removeBikeWithoutReservationFrom(station)) {
            if (user.decidesToReturnBike()) {  // user goes directly to another station to return his bike
                newEvents = manageSlotReservationDecisionAtOtherStation();
            } else {   // user rides his bike to a point which is not a station
                GeoPoint point = user.decidesNextPoint();
                user.setDestination(point);
                int arrivalTime = user.timeToReach();
                newEvents.add(new EventUserWantsToReturnBike(getInstant() + arrivalTime, user, point));
            }
        } else {   // there're not bikes: user decides to go to another station, to reserve a bike or to leave the simulation
            user.getMemory().update(UserMemory.FactType.BIKES_UNAVAILABLE);
            if (user.decidesToLeaveSystemWhenBikesUnavailable(instant)) {
                user.setPosition(null);
            } else {
                newEvents = manageBikeReservationDecisionAtOtherStation();
            }
        }
        return newEvents;
    }

    @Override
    public List<Entity> getEntities() {
        return entities;
    }
}
