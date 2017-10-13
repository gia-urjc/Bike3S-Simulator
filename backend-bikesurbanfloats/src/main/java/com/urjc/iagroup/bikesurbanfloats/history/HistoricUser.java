package com.urjc.iagroup.bikesurbanfloats.history;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.urjc.iagroup.bikesurbanfloats.entities.Person;
import com.urjc.iagroup.bikesurbanfloats.entities.models.UserModel;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;

public class HistoricUser implements HistoricEntity<HistoricUser>, UserModel<HistoricBike, HistoricStation> {

	private int id;

	private GeoPoint position;

	private HistoricBike bike;

	private double averageVelocity;

	private boolean reservedBike;
	private boolean reservedSlot;

	private HistoricStation destinationStation;


	HistoricUser(Person user) {
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
	public JsonObject getChanges(HistoricUser previousSelf) {
		JsonObject changes = HistoricEntity.super.getChanges(previousSelf);

		if (changes == null) return null;

		boolean hasChanges = false;

		changes.add("id", new JsonPrimitive(id));

		JsonObject bike = HistoricEntity.idChange(previousSelf.bike, this.bike);
	
		if (bike != null) {
			changes.add("bike", bike);
			hasChanges = true;
		}
	
		if (!this.position.equals(previousSelf.position)) {
			double dlat = this.position.getLatitude() - previousSelf.position.getLatitude();
			double dlon = this.position.getLongitude() - previousSelf.position.getLongitude();
			changes.add("position", History.gson.toJsonTree(new GeoPoint(dlat, dlon)));
			hasChanges = true;
		}
	
		// TODO: implement other possible changes like destination
	
	    return hasChanges ? changes : null;
	}
}
