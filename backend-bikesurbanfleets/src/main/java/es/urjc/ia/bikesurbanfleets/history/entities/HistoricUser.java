package es.urjc.ia.bikesurbanfleets.history.entities;

import com.google.gson.annotations.Expose;
import es.urjc.ia.bikesurbanfleets.entities.Bike;
import es.urjc.ia.bikesurbanfleets.entities.Station;
import es.urjc.ia.bikesurbanfleets.entities.users.AssociatedType;
import es.urjc.ia.bikesurbanfleets.entities.users.User;
import es.urjc.ia.bikesurbanfleets.entities.users.UserType;
import es.urjc.ia.bikesurbanfleets.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.graphs.GeoRoute;
import es.urjc.ia.bikesurbanfleets.history.HistoricEntity;
import es.urjc.ia.bikesurbanfleets.history.History.IdReference;
import es.urjc.ia.bikesurbanfleets.history.JsonIdentifier;

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
    private double walkingVelocity;

    @Expose
    private double cyclingVelocity;

    private GeoPoint position;
    private GeoRoute route;
    private IdReference bike;
    private IdReference destinationStation;

    @Expose
    private UserType type;

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
    }

    @Override
    public int getId() {
        return id;
    }
}
