package com.urjc.iagroup.bikesurbanfloats.entities;

public abstract class Entity {

    private int id;

    public Entity(int id) {
        this.id = id;
    }
    
    public Entity(Entity entity) {
    	this.id = entity.id;
    }

    public int getId() {
        return this.id;
    }

}
