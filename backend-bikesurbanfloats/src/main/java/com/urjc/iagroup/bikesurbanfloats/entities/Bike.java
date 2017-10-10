package com.urjc.iagroup.bikesurbanfloats.entities;

import com.urjc.iagroup.bikesurbanfloats.entities.models.BikeModel;

public class Bike implements Entity, BikeModel {
    private int id;

    private boolean reserved;

    public Bike(int id) {
        this.id = id;
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
