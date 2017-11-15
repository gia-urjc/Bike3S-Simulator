package com.urjc.iagroup.bikesurbanfloats.history.entities;

import com.google.gson.annotations.Expose;
import com.urjc.iagroup.bikesurbanfloats.entities.Bike;
import com.urjc.iagroup.bikesurbanfloats.history.HistoricEntity;
import com.urjc.iagroup.bikesurbanfloats.history.JsonIdentifier;

/**
 * It contains the rlevant information of a specific bike, e. g, its history.
 * @author IAgroup
 *
 */
@JsonIdentifier("bikes")
public class HistoricBike implements HistoricEntity {

    @Expose
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
}
