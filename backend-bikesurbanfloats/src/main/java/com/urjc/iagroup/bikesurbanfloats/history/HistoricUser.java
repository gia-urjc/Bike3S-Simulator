package com.urjc.iagroup.bikesurbanfloats.history;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.urjc.iagroup.bikesurbanfloats.entities.User;
import com.urjc.iagroup.bikesurbanfloats.entities.models.UserModel;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;

import java.util.HashMap;
import java.util.Map;

public class HistoricUser implements HistoricEntity<HistoricUser>, UserModel<HistoricBike, HistoricStation> {

	private int id;

	private GeoPoint position;

	private HistoricBike bike;

	private double averageVelocity;

	private boolean reservedBike;
	private boolean reservedSlot;

	private HistoricStation destinationStation;


	HistoricUser(User user) {
	    this.id = user.getId();
	    this.position = new GeoPoint(user.getPosition());

        this.bike = user.getBike() == null ? null: new HistoricBike(user.getBike());

        this.averageVelocity = user.getAverageVelocity();
        this.reservedBike = user.hasReservedBike();
        this.reservedSlot = user.hasReservedSlot();

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
    public boolean hasBike() {
        return this.bike != null;
    }

    @Override
    public boolean hasReservedBike() {
        return reservedBike;
    }

    @Override
    public boolean hasReservedSlot() {
        return reservedSlot;
    }

    @Override
    public HistoricStation getDestinationStation() {
        return destinationStation;
    }

    @Override
    public double getAverageVelocity() {
        return averageVelocity;
    }

    @Override
	public JsonObject makeChangeEntryFrom(HistoricUser previousSelf) {
		JsonObject changeEntry = HistoricEntity.super.makeChangeEntryFrom(previousSelf);

		if (changeEntry == null) return null;

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
