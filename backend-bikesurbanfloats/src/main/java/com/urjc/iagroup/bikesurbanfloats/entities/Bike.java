package com.urjc.iagroup.bikesurbanfloats.entities;

import com.google.gson.annotations.Expose;
import com.urjc.iagroup.bikesurbanfloats.entities.models.BikeModel;
import com.urjc.iagroup.bikesurbanfloats.history.entities.HistoricBike;
import com.urjc.iagroup.bikesurbanfloats.history.HistoryReference;
import com.urjc.iagroup.bikesurbanfloats.util.IdGenerator;

@HistoryReference(HistoricBike.class)
public class Bike implements Entity, BikeModel {

    private static IdGenerator idGenerator = new IdGenerator();

    @Expose
    private int id;

    @Expose
    private boolean reserved;

    public Bike() {
        this.id = idGenerator.next();
        this.reserved = false;
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
    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }
}
