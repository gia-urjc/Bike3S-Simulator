package es.urjc.ia.bikesurbanfleets.infraestructure.entities;

import es.urjc.ia.bikesurbanfleets.common.interfaces.Entity;
import es.urjc.ia.bikesurbanfleets.common.util.IdGenerator;

/**
 * This is the main entity with which users and stations interact
 * It represents bike state: reserved or available (not reserved)
 * @author IAgroup
 *
 */
public class Bike implements Entity {

    public static IdGenerator idgenerator;
    
    public static void resetIdGenerator(){
        idgenerator=new IdGenerator();
    }
    

    private int id;
    private boolean reserved;

    public Bike() {
        this.id  = idgenerator.next();
        this.reserved = false;
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
    
    @Override
    public String toString() {
        String result = this.getClass().getSimpleName()+" : | Id: " + getId();
        result += " | Reserved: " + isReserved();
        return result;
    }

}
