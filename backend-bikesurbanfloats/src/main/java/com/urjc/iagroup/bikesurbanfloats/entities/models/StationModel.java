package com.urjc.iagroup.bikesurbanfloats.entities.models;

import java.util.List;

import com.urjc.iagroup.bikesurbanfloats.graphs.GeoPoint;

public interface StationModel<B extends BikeModel> {

    GeoPoint getPosition();

    int getCapacity();

    List<B> getBikes();

    int getReservedBikes();

    int getReservedSlots();

    int availableBikes();

    int availableSlots();

}
