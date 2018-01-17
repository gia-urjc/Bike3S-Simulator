package es.urjc.ia.bikesurbanfleets.entities.history;

import com.google.gson.annotations.Expose;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoRoute;
import es.urjc.ia.bikesurbanfleets.entities.Bike;
import es.urjc.ia.bikesurbanfleets.entities.Reservation;
import es.urjc.ia.bikesurbanfleets.entities.Station;
import es.urjc.ia.bikesurbanfleets.entities.User;
import es.urjc.ia.bikesurbanfleets.common.interfaces.HistoricEntity;
import es.urjc.ia.bikesurbanfleets.history.History.IdReference;
import es.urjc.ia.bikesurbanfleets.history.JsonIdentifier;
import es.urjc.ia.bikesurbanfleets.users.AssociatedType;
import es.urjc.ia.bikesurbanfleets.users.UserType;

import java.util.stream.Collectors;

/**
 * It contains the relevant information of a specific user, e. g., its history.
 * @author IAgroup
 *
 */
@JsonIdentifier("users")
public class HistoricUser implements HistoricEntity {

    @Expose
    private int id;

    @Expose
    private UserType type;

    @Expose
    private double walkingVelocity;

    @Expose
    private double cyclingVelocity;

    @Expose
    private IdReference reservations;

    private GeoPoint position;
    private GeoRoute route;
    private IdReference bike;
    private IdReference destinationStation;

    public HistoricUser(User user) {
        Bike bike = user.getBike();
        Station station = user.getDestinationStation();

        this.id = user.getId();
        this.position = user.getPosition() == null ? null : new GeoPoint(user.getPosition());
        this.bike = bike == null ? null : new IdReference(HistoricBike.class, bike.getId());
        this.walkingVelocity = user.getWalkingVelocity();
        this.cyclingVelocity = user.getCyclingVelocity();
        this.destinationStation = station == null ? null : new IdReference(HistoricStation.class, station.getId());
        this.route = user.getRoute();
        this.type = user.getClass().getAnnotation(AssociatedType.class).value();
        this.reservations = new IdReference(HistoricReservation.class, user.getReservations().stream().map(Reservation::getId).collect(Collectors.toList()));
    }

    @Override
    public int getId() {
        return id;
    }
}
