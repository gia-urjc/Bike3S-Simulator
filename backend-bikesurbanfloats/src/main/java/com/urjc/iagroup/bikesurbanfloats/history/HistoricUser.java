package com.urjc.iagroup.bikesurbanfloats.history;

import com.urjc.iagroup.bikesurbanfloats.entities.Bike;
import com.urjc.iagroup.bikesurbanfloats.entities.Person;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;

class HistoricUser extends Person {

    private HistoricBike bike;

    HistoricUser(Person user) {
        super(user.getId(), new GeoPoint(user.getPosition()));

        this.bike = user.getBike() == null ? null : new HistoricBike(user.getBike());
    }

    @Override
    public Bike getBike() {
        return this.bike;
    }

    @Override
    public Station determineStation() {
        return null;
    }

    @Override
    public boolean decidesToReserveBike(Station station) {
        return false;
    }

    @Override
    public boolean decidesToReserveSlot(Station station) {
        return false;
    }

    @Override
    public GeoPoint decidesNextPoint() {
        return null;
    }

    @Override
    public boolean decidesToReturnBike() {
        return false;
    }
}
