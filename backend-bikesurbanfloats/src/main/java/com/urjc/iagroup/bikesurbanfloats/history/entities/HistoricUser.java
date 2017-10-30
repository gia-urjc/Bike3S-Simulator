package com.urjc.iagroup.bikesurbanfloats.history.entities;

import com.google.gson.annotations.JsonAdapter;
import com.urjc.iagroup.bikesurbanfloats.entities.User;
import com.urjc.iagroup.bikesurbanfloats.entities.models.UserModel;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.history.HistoricEntity;
import com.urjc.iagroup.bikesurbanfloats.history.IdReferenceAdapter;
import com.urjc.iagroup.bikesurbanfloats.history.JsonIdentifier;

@JsonIdentifier("users")
public class HistoricUser implements HistoricEntity, UserModel<HistoricBike, HistoricStation> {

	private int id;

	private GeoPoint position;

    private double walkingVelocity;
    private double cyclingVelocity;

    @JsonAdapter(IdReferenceAdapter.class)
	private HistoricBike bike;

    @JsonAdapter(IdReferenceAdapter.class)
	private HistoricStation destinationStation;

    public HistoricUser(User user) {
        this.id = user.getId();
        this.position = user.getPosition() == null? null : new GeoPoint(user.getPosition());
        this.bike = user.getBike() == null ? null: new HistoricBike(user.getBike());
        this.walkingVelocity = user.getWalkingVelocity();
        this.cyclingVelocity = user.getCyclingVelocity();
        this.destinationStation = user.getDestinationStation() == null ? null : new HistoricStation(user.getDestinationStation());
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public GeoPoint getPosition() {
        return position;
    }

    @Override
    public HistoricBike getBike() {
        return bike;
    }

    @Override
    public HistoricStation getDestinationStation() {
        return destinationStation;
    }

    @Override
    public double getWalkingVelocity() {
        return walkingVelocity;
    }

    @Override
    public double getCyclingVelocity() {
        return cyclingVelocity;
    }
}
