package com.urjc.iagroup.bikesurbanfloats.history.entities;

import com.urjc.iagroup.bikesurbanfloats.entities.User;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.history.HistoricEntity;
import com.urjc.iagroup.bikesurbanfloats.history.JsonIdentifier;

@JsonIdentifier("users")
public class HistoricUser implements HistoricEntity {

    private int id;
    private GeoPoint position;

    private double walkingVelocity;
    private double cyclingVelocity;

    private Integer bike;
    private Integer destinationStation;

    public HistoricUser(User user) {
        this.id = user.getId();
        this.position = user.getPosition() == null ? null : new GeoPoint(user.getPosition());
        this.bike = user.getBike() == null ? null : user.getBike().getId();
        this.walkingVelocity = user.getWalkingVelocity();
        this.cyclingVelocity = user.getCyclingVelocity();
        this.destinationStation = user.getDestinationStation() == null ? null : user.getDestinationStation().getId();
    }

    @Override
    public int getId() {
        return id;
    }
}
