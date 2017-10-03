package com.urjc.iagroup.bikesurbanfloats.history;

import com.urjc.iagroup.bikesurbanfloats.entities.Bike;

class HistoricBike extends Bike {

    HistoricBike(Bike bike) {
        super(bike.getId());
        this.setReserved(bike.isReserved());
    }
}
