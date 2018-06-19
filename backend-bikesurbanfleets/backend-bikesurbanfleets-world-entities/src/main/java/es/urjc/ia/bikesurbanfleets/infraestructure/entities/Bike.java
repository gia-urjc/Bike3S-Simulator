package es.urjc.ia.bikesurbanfleets.infraestructure.entities;

import es.urjc.ia.bikesurbanfleets.common.interfaces.Entity;
import es.urjc.ia.bikesurbanfleets.common.util.IdGenerator;
import es.urjc.ia.bikesurbanfleets.history.entities.HistoricBike;
import es.urjc.ia.bikesurbanfleets.history.History;
import es.urjc.ia.bikesurbanfleets.history.HistoryReference;

/**
 * This is the main entity with which users and stations interact
 * It represents bike state: reserved or available (not reserved)
 * @author IAgroup
 *
 */
@HistoryReference(HistoricBike.class)
public class Bike implements Entity {

    private static IdGenerator idGenerator = new IdGenerator();

    private int id;
    private boolean reserved;

    public Bike() {
        this.id  = idGenerator.next();
        this.reserved = false;
        History.registerEntity(this);
    }

    @Override
    public int getId() {
        return id;
    }

    public boolean isReserved() {
        return reserved;
    }

    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }
}
