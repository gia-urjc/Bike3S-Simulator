package com.urjc.iagroup.bikesurbanfloats.history.entities;

import com.google.gson.annotations.Expose;
import com.urjc.iagroup.bikesurbanfloats.entities.Bike;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.entities.users.AssociatedType;
import com.urjc.iagroup.bikesurbanfloats.entities.users.User;
import com.urjc.iagroup.bikesurbanfloats.entities.users.UserType;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoRoute;
import com.urjc.iagroup.bikesurbanfloats.history.HistoricEntity;
import com.urjc.iagroup.bikesurbanfloats.history.History.IdReference;
import com.urjc.iagroup.bikesurbanfloats.history.JsonIdentifier;

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
