package es.urjc.ia.bikesurbanfleets.history.entities;

import com.google.gson.annotations.Expose;

import es.urjc.ia.bikesurbanfleets.history.JsonIdentifier;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Bike;

/**
 * It contains the rlevant information of a specific bike, e. g, its history.
 * @author IAgroup
 *
 */
@JsonIdentifier("bikes")
public class HistoricBike implements HistoricEntity {

    @Expose
    private int id;
    
  //  @Expose
  //  private boolean reserved;

    public HistoricBike(Bike bike) {
        this.id = bike.getId();
  //      this.reserved = bike.isReserved();
    }

    @Override
    public int getId() {
        return id;
    }
}
