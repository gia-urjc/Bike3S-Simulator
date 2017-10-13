package com.urjc.iagroup.bikesurbanfloats.history;

import com.google.gson.JsonObject;
import com.urjc.iagroup.bikesurbanfloats.entities.Bike;
import com.urjc.iagroup.bikesurbanfloats.entities.models.BikeModel;

public class HistoricBike implements HistoricEntity<HistoricBike>, BikeModel {

	private int id;

	private boolean reserved;

	HistoricBike(Bike bike) {
		this.id = bike.getId();
		this.reserved = bike.isReserved();
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public boolean isReserved() {
		return reserved;
	}

	@Override
	public void setReserved(boolean value) {
		reserved = value;
	}

	@Override
	public JsonObject getChanges(HistoricBike previousSelf) {
		// TODO: serialize changes
		return null;
	}

}
