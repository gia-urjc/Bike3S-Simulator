package com.urjc.iagroup.bikesurbanfloats.history;

import com.urjc.iagroup.bikesurbanfloats.entities.Station;

import java.util.LinkedList;
import java.util.stream.Collectors;

class HistoricStation extends Station {

    HistoricStation(Station station) {
        super(
                station.getId(),
                station.getPosition(),
                station.getCapacity(),
                station.getBikes().stream().map(HistoricBike::new).collect(Collectors.toCollection(LinkedList::new))
        );
    }
}
