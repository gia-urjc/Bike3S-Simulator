package es.urjc.ia.bikesurbanfleets.core.config.entrypoints;

import es.urjc.ia.bikesurbanfleets.core.events.EventUserAppears;

import java.util.List;

/**
 * It is an event generator for user appearances.
 * It represents an entry point at system geographic map where a unique user or several users 
 * appear and start interacting with the system. 
 * @author IAgroup
 *
 */
public abstract class EntryPoint {
    
    public static int TOTAL_SIMULATION_TIME;
    
    /**
     * It generates user appearance events, which are the main events that starts the simulation execution.
     * @return
     */
    public abstract List<EventUserAppears> generateEvents();
    
}
