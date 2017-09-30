package com.urjc.iagroup.bikesurbanfloats.history;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
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

    public JsonObject getChanges(HistoricUser previous) {
        if (previous == null) return null;

        JsonObject changes = new JsonObject();
        boolean hasChanges = false;

        changes.add("id", new JsonPrimitive(this.getId()));

        if (this.bike == null && previous.bike != null) {
            changes.add("bike", new JsonPrimitive(-1));
            hasChanges = true;
        } else if (this.bike != null && (previous.bike == null || this.bike.getId() != previous.bike.getId())) {
            changes.add("bike", new JsonPrimitive(this.bike.getId()));
            hasChanges = true;
        }

        if (!this.getPosition().equals(previous.getPosition())) {
            double dlat = this.getPosition().getLatitude() - previous.getPosition().getLatitude();
            double dlon = this.getPosition().getLongitude() - previous.getPosition().getLongitude();
            changes.add("position", History.gson.toJsonTree(new GeoPoint(dlat, dlon)));
            hasChanges = true;
        }

        // TODO: implement other possible changes like destination

        return hasChanges ? changes : null;
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
