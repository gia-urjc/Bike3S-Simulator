package com.urjc.iagroup.bikesurbanfloats.history.entities;

import com.google.gson.JsonObject;
import com.urjc.iagroup.bikesurbanfloats.entities.Bike;
import com.urjc.iagroup.bikesurbanfloats.entities.models.BikeModel;
import com.urjc.iagroup.bikesurbanfloats.history.HistoricEntity;
import com.urjc.iagroup.bikesurbanfloats.history.JsonIdentifier;

@JsonIdentifier("bikes")
public class HistoricBike implements HistoricEntity<HistoricBike>, BikeModel {

	private int id;

	private boolean reserved;

	public HistoricBike(Bike bike) {
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
	public JsonObject makeChangeEntryFrom(HistoricBike previousSelf) {
		// TODO: serialize changes
		return null;
	}

}
