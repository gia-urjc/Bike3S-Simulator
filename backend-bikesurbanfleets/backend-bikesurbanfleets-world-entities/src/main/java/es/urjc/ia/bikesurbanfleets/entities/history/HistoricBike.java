package es.urjc.ia.bikesurbanfleets.entities.history;

import com.google.gson.annotations.Expose;
import es.urjc.ia.bikesurbanfleets.entities.Bike;
import es.urjc.ia.bikesurbanfleets.common.interfaces.HistoricEntity;
import es.urjc.ia.bikesurbanfleets.history.JsonIdentifier;

/**
 * It contains the rlevant information of a specific bike, e. g, its history.
 * @author IAgroup
 *
 */
@JsonIdentifier("bikes")
public class HistoricBike implements HistoricEntity {

    @Expose
    private int id;

    private boolean reserved;

    public HistoricBike(Bike bike) {
        this.id = bike.getId();
        this.reserved = bike.isReserved();
    }

    @Override
    public int getId() {
        return id;
    }
}
