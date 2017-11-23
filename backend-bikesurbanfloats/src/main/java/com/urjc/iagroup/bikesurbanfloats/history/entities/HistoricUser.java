package com.urjc.iagroup.bikesurbanfloats.history.entities;

import com.google.gson.annotations.Expose;
import com.urjc.iagroup.bikesurbanfloats.entities.users.AssociatedType;
import com.urjc.iagroup.bikesurbanfloats.entities.users.User;
import com.urjc.iagroup.bikesurbanfloats.entities.users.UserType;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoRoute;
import com.urjc.iagroup.bikesurbanfloats.history.HistoricEntity;
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
    private Integer bike;
    private Integer destinationStation;
    private GeoRoute route;

    @Expose
    private UserType type;

    public HistoricUser(User user) {
        this.id = user.getId();
        this.position = user.getPosition() == null ? null : new GeoPoint(user.getPosition());
        this.bike = user.getBike() == null ? null : user.getBike().getId();
        this.walkingVelocity = user.getWalkingVelocity();
        this.cyclingVelocity = user.getCyclingVelocity();
        this.destinationStation = user.getDestinationStation() == null ? null : user.getDestinationStation().getId();
        this.route = user.getCurrentRoute();
        this.type = user.getClass().getAnnotation(AssociatedType.class).value();
    }

    @Override
    public int getId() {
        return id;
    }
}
