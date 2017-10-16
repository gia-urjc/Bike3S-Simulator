package com.urjc.iagroup.bikesurbanfloats.entities.models;

import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;

import java.util.List;

public interface StationModel<BikeType extends BikeModel> {

    GeoPoint getPosition();

    int getCapacity();

    List<BikeType> getBikes();

    int getReservedBikes();

    int getReservedSlots();

    int availableBikes();

    int availableSlots();

}
