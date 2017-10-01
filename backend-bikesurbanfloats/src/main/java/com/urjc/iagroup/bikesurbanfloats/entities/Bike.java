package com.urjc.iagroup.bikesurbanfloats.entities;

public class Bike extends Entity {

    private boolean reserved;

    public Bike(int id) {
        super(id);
        this.reserved = false;
    }

    public boolean isReserved() {
        return reserved;
    }

    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }
}
