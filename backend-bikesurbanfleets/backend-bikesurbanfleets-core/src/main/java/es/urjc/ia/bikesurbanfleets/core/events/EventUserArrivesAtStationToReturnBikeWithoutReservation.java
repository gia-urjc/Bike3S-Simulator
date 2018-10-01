package es.urjc.ia.bikesurbanfleets.core.events;

import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.common.util.MessageGuiFormatter;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoRoute;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Entity;
import es.urjc.ia.bikesurbanfleets.users.User;
import es.urjc.ia.bikesurbanfleets.users.UserMemory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventUserArrivesAtStationToReturnBikeWithoutReservation extends EventUser {
    private List<Entity> entities;
    private Station station;

    public EventUserArrivesAtStationToReturnBikeWithoutReservation(int instant, User user, Station station) {
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
            user.setInstant(this.instant);
            debugEventLog();
            if(!user.returnBikeWithoutReservationTo(station)) {
                user.getMemory().update(UserMemory.FactType.SLOTS_UNAVAILABLE);
                user.setPosition(station.getPosition());
                debugEventLog("User can't return bike. Station info: " + station.toString()) ;
                newEvents = manageSlotReservationDecisionAtOtherStation();
            } else {
                user.setDestinationPoint(user.getDestinationPlace());
                user.setDestinationStation(null);
                GeoRoute route = user.determineRoute();
                user.setRoute(route);
                int arrivalTime = user.timeToReach();
                debugEventLog("User returns the bike. Station info: " + station.toString());
                newEvents.add(new EventUserArrivesAtDestinationInCity(arrivalTime, user));	
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
