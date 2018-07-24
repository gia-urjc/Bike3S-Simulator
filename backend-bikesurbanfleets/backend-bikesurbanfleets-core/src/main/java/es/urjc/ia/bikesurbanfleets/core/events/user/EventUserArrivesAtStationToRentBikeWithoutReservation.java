package es.urjc.ia.bikesurbanfleets.core.events.user;

import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.common.util.MessageGuiFormatter;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoRoute;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Entity;
import es.urjc.ia.bikesurbanfleets.users.User;
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
    public List<Event> execute() {
        List<Event> newEvents = new ArrayList<>();
        try {
            user.setInstant(getInstant());
            user.setPosition(station.getPosition());
            debugEventLog();
            if (user.removeBikeWithoutReservationFrom(station)) {
                debugEventLog("User removes Bike without reservation");
                if (user.decidesToReturnBike()) {  // user goes directly to another station to return his bike
                    debugEventLog("User decides to return bike to other station");
                    newEvents = manageSlotReservationDecisionAtOtherStation();
                } else {   // user rides his bike to a point which is not a station
                    GeoPoint point = user.decidesNextPoint();
                    user.setDestinationPoint(point);
                    user.setDestinationStation(null);
                    GeoRoute route = user.determineRoute();
                    user.setRoute(route);
                    int arrivalTime = user.timeToReach();
                    debugEventLog("User decides to take a ride");
                    newEvents.add(new EventUserWantsToReturnBike(getInstant() + arrivalTime, user, point));
                }
            } else {   // there're not bikes: user decides to go to another station, to reserve a bike or to leave the simulation
                user.getMemory().update(UserMemory.FactType.BIKES_UNAVAILABLE);
                debugEventLog("User can't take bikes from the station");
                if (user.decidesToLeaveSystemWhenBikesUnavailable()) {
                    user.leaveSystem();
                    debugEventLog("User decides to leave the system");
                } else {
                    newEvents = manageBikeReservationDecisionAtOtherStation();
                }
            }
        }
        catch(Exception e) {
            exceptionTreatment(e);
        }

        return newEvents;
    }

    @Override
    public List<Entity> getEntities() {
        return entities;
    }
}
