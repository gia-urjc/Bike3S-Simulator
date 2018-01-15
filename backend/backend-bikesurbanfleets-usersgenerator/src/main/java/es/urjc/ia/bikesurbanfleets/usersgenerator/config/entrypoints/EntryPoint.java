package es.urjc.ia.bikesurbanfleets.usersgenerator.config.entrypoints;

import es.urjc.ia.bikesurbanfleets.usersgenerator.config.SingleUser;

import java.util.List;

/**
 * It is an event generator for user appearances.
 * It represents an entry point at system geographic map where a unique user or several users
 * appear and start interacting with the system.
 * @author IAgroup
 *
 */
public abstract class EntryPoint {

    public enum EntryPointType {
        POISSON, RANDOM, SINGLEUSER
    }

    private EntryPointType entryPointType;

    public static int TOTAL_SIMULATION_TIME;
    /**
     * It generate single users for the configuration file,
     * which are the main events that starts the simulation execution.
     * @return a list of single users
     */

    public EntryPointType getEntryPointType() {
        return entryPointType;
    }

    public abstract List<SingleUser> generateUsers();

}