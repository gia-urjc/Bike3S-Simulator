/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.history;

import es.urjc.ia.bikesurbanfleets.common.interfaces.Entity;
import es.urjc.ia.bikesurbanfleets.history.entities.HistoricEntity;
import es.urjc.ia.bikesurbanfleets.history.entities.HistoricBike;
import es.urjc.ia.bikesurbanfleets.history.entities.HistoricFleetManager;
import es.urjc.ia.bikesurbanfleets.history.entities.HistoricReservation;
import es.urjc.ia.bikesurbanfleets.history.entities.HistoricStation;
import es.urjc.ia.bikesurbanfleets.history.entities.HistoricUser;
import es.urjc.ia.bikesurbanfleets.services.fleetManager.FleetManager;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Bike;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Reservation;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.worldentities.users.User;

/**
 *
 * @author holger
 */
public class EntityHistoricEntityMapping {
      /**
     * It finds out, from an entity class, the corresponding historical class.
     *
     * @param entityClass It is the entity class whose corresponding history
     * class musts be found out.
     * @return the corresponding history class to the entity class.
     */
    static Class<? extends HistoricEntity> getHistoricEntityClass(Entity e) {
        Class<? extends Entity> entityclass = e.getClass();

        while (!entityclass.getSuperclass().equals(Object.class)) {
            entityclass = (Class<? extends Entity>) entityclass.getSuperclass();
        }

        if (entityclass==Bike.class) return HistoricBike.class;
        if (entityclass==User.class) return HistoricUser.class;
        if (entityclass==Reservation.class) return HistoricReservation.class;
        if (entityclass==Station.class) return HistoricStation.class;
        if (entityclass==FleetManager.class) return HistoricFleetManager.class;
        // else there is no mapping
        throw new IllegalStateException("no historic class found for the entity:" + e);
    }

    static boolean includeInHistory(Entity e) {
        Class<? extends Entity> entityclass = e.getClass();

        while (!entityclass.getSuperclass().equals(Object.class)) {
            entityclass = (Class<? extends Entity>) entityclass.getSuperclass();
        }
        if (entityclass==Bike.class) return false;
        if (entityclass==User.class) return true;
        if (entityclass==Reservation.class) return false;
        if (entityclass==Station.class) return true;
        if (entityclass==FleetManager.class) return true;
        // else this entity does not exist
        throw new IllegalStateException("no historic class found for the entity:" + e);
    }
    
}
