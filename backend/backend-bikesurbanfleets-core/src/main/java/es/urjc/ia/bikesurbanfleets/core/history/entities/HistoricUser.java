package es.urjc.ia.bikesurbanfleets.core.history.entities;

import com.google.gson.annotations.Expose;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoRoute;
import es.urjc.ia.bikesurbanfleets.core.history.HistoricEntity;
import es.urjc.ia.bikesurbanfleets.core.history.History;
import es.urjc.ia.bikesurbanfleets.core.history.JsonIdentifier;
import es.urjc.ia.bikesurbanfleets.core.entities.Bike;
import es.urjc.ia.bikesurbanfleets.core.entities.Station;
import es.urjc.ia.bikesurbanfleets.core.entities.users.AssociatedType;
import es.urjc.ia.bikesurbanfleets.core.entities.users.User;
import es.urjc.ia.bikesurbanfleets.core.entities.users.UserType;

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
    private History.IdReference bike;
    private History.IdReference destinationStation;

    @Expose
    private UserType type;

    public HistoricUser(User user) {
        Bike bike = user.getBike();
        Station station = user.getDestinationStation();

        this.id = user.getId();
        this.position = user.getPosition() == null ? null : new GeoPoint(user.getPosition());
        this.bike = bike == null ? null : new History.IdReference(HistoricBike.class, bike.getId());
        this.walkingVelocity = user.getWalkingVelocity();
        this.cyclingVelocity = user.getCyclingVelocity();
        this.destinationStation = station == null ? null : new History.IdReference(HistoricStation.class, station.getId());
        this.route = user.getRoute();
        this.type = user.getClass().getAnnotation(AssociatedType.class).value();
    }

    @Override
    public int getId() {
        return id;
    }
}
