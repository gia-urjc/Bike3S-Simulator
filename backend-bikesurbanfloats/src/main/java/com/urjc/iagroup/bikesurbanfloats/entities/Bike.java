package com.urjc.iagroup.bikesurbanfloats.entities;

import com.urjc.iagroup.bikesurbanfloats.util.IdGenerator;

public class Bike {
	
    private int id;
    private boolean reserved;

    public Bike(int id) {
        this.id = id;
        this.reserved = false;
    }

    public boolean isReserved() {
        return reserved;
    }

    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }

    public int getId() {
        return id;
    }
}
