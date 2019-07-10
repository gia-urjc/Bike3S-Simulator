package es.urjc.ia.bikesurbanfleets.history.entities;

import com.google.gson.annotations.Expose;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoRoute;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Bike;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Reservation;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.history.JsonIdentifier;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserType;
import es.urjc.ia.bikesurbanfleets.worldentities.users.User;

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

//    @Expose
    private String type;

    @Expose
    private double walkingVelocity;

    @Expose
    private double cyclingVelocity;

//    @Expose
//    private User.STATE state;
   
//    @Expose
//    private IdReference reservation;
     
    @Expose
    private boolean hasreservation;
   
    @Expose
    private GeoPoint position;
    @Expose
    private GeoPoint destinationLocation;

    @Expose
    private GeoRoute route;
//    @Expose
//    private IdReference bike;
    @Expose
    private boolean hasbike;

//    @Expose
//    private IdReference destinationStation;

    public HistoricUser(User user) {
        Bike b = user.getBike();
        Station station = user.getDestinationStation();
        Reservation res=user.getReservation();

  //      this.state=user.getState();
        this.id = user.getId();
        this.position = user.getPosition() == null ? null : new GeoPoint(user.getPosition());
  //      this.bike = b == null ? null : new IdReference(HistoricBike.class, b.getId());
        this.hasbike= b == null ? false : true;
        this.walkingVelocity = user.getWalkingVelocity();
        this.cyclingVelocity = user.getCyclingVelocity();
  //      this.destinationStation = station == null ? null : new IdReference(HistoricStation.class, station.getId());
        this.route = user.getRoute();
        this.type = user.getClass().getAnnotation(UserType.class).value();
  //      this.reservation = res == null ? null : new IdReference(HistoricReservation.class, res.getId());
        this.hasreservation= res == null ? false : true;
        this.destinationLocation=user.getDestinationPlace();
    }

    @Override
    public int getId() {
        return id;
    }
}
