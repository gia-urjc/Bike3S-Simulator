package com.urjc.iagroup.bikesurbanfloats.history.entities;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.urjc.iagroup.bikesurbanfloats.entities.User;
import com.urjc.iagroup.bikesurbanfloats.entities.models.UserModel;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.history.HistoricEntity;
import com.urjc.iagroup.bikesurbanfloats.history.JsonIdentifier;

import java.util.HashMap;
import java.util.Map;

@JsonIdentifier("users")
public class HistoricUser implements HistoricEntity<HistoricUser>, UserModel<HistoricBike, HistoricStation> {

	private int id;

	private GeoPoint position;

	private HistoricBike bike;

	private double walkingVelocity;
	private double cyclingVelocity;

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

    @Override
	public JsonObject makeChangeEntryFrom(HistoricUser previousSelf) {
		JsonObject changeEntry = new JsonObject();

		JsonObject changes = new JsonObject();

        Map<String, JsonObject> properties = new HashMap<>();

        properties.put("bike", HistoricEntity.idReferenceChange(previousSelf.bike, this.bike));
        properties.put("position", HistoricEntity.propertyChange(previousSelf.position, this.position));

		// TODO: add other possible changes like destination

        properties.forEach((property, json) -> {
            if (!json.entrySet().isEmpty()) {
                changes.add(property, json);
            }
        });

        if (changes.entrySet().isEmpty()) return null;

        changeEntry.add("id", new JsonPrimitive(id));
        changeEntry.add("changes", changes);
	
	    return changeEntry;
	}
}
