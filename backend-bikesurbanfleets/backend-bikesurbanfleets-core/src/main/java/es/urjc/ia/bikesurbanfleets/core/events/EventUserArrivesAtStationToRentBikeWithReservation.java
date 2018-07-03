package es.urjc.ia.bikesurbanfleets.core.events;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoRoute;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.common.util.MessageGuiFormatter;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Reservation;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Entity;
import es.urjc.ia.bikesurbanfleets.users.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventUserArrivesAtStationToRentBikeWithReservation extends EventUser {

    private List<Entity> entities;
    private Station station;
    private Reservation reservation;

    public EventUserArrivesAtStationToRentBikeWithReservation(int instant, User user, Station station, Reservation reservation) {
        super(instant, user);
        this.entities = new ArrayList<>(Arrays.asList(user, station, reservation));
        this.station = station;
        this.reservation = reservation;
    }
    
    public Station getStation() {
        return station;
    }

    public Reservation getReservation() {
        return reservation;
    }

    @Override
    public List<Event> execute() {
        List<Event> newEvents = new ArrayList<>();
        try {
            user.setInstant(this.instant);
            user.setPosition(station.getPosition());
            reservation.resolve(instant);
            user.removeBikeWithReservationFrom(station);
            debugEventLog("User removes Bike with reservation");
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
                debugEventLog("User decides take a ride");
                newEvents.add(new EventUserWantsToReturnBike(getInstant() + arrivalTime, user, point));
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