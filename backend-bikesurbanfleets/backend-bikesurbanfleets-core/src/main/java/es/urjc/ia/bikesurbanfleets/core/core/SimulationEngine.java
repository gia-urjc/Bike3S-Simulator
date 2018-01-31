package es.urjc.ia.bikesurbanfleets.core.core;


import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.core.config.GlobalInfo;
import es.urjc.ia.bikesurbanfleets.core.config.StationsInfo;
import es.urjc.ia.bikesurbanfleets.core.config.UsersInfo;
import es.urjc.ia.bikesurbanfleets.core.events.EventUser;
import es.urjc.ia.bikesurbanfleets.core.events.EventUserAppears;
import es.urjc.ia.bikesurbanfleets.entities.Reservation;
import es.urjc.ia.bikesurbanfleets.entities.User;
import es.urjc.ia.bikesurbanfleets.history.History;
import es.urjc.ia.bikesurbanfleets.log.Debug;
import es.urjc.ia.bikesurbanfleets.systemmanager.SystemManager;
import es.urjc.ia.bikesurbanfleets.users.UserFactory;
import es.urjc.ia.bikesurbanfleets.usersgenerator.SingleUser;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * This is the most important class which makes the simulation possible.
 * It uses a queue to manage the events proccessing.
 * @author IAgroup
 *
 */
public class SimulationEngine {

    private PriorityQueue<Event> eventsQueue = new PriorityQueue<>();
    private GlobalInfo globalInfo;
    private StationsInfo stationsInfo;
    private UsersInfo usersInfo;
    private SystemManager systemManager;

    /**
     * It creates an event queue where its events are sorted by the time instant when they'll occur.
     */
    public SimulationEngine(GlobalInfo globalInfo, StationsInfo stationsInfo, UsersInfo usersInfo,
                            SystemManager systemManager) {
        this.globalInfo = globalInfo;
        this.stationsInfo = stationsInfo;
        this.usersInfo = usersInfo;
        this.systemManager = systemManager;
        this.eventsQueue = new PriorityQueue<>(processUsers());
        Reservation.VALID_TIME = globalInfo.getReservationTime();
        Debug.DEBUG_MODE = globalInfo.isDebugMode();
    }

    private List<EventUserAppears> processUsers() {
        List<EventUserAppears> eventUserAppearsList = new ArrayList<>();
        UserFactory userFactory = new UserFactory();
        for (SingleUser singleUser: usersInfo.getUsers()) {
            User user = userFactory.createUser(singleUser.getUserType());
            int instant = singleUser.getTimeInstant();
            GeoPoint position = singleUser.getPosition();
            eventUserAppearsList.add(new EventUserAppears(instant, user, position));
        }

        eventUserAppearsList.stream()
                .map(EventUser::getUser)
                .forEach(user -> user.setSystemManager(systemManager));

        return eventUserAppearsList;
    }

    public void run() throws Exception {

        History.init(globalInfo.getHistoryOutputPath());
        Debug.init();

        while (!eventsQueue.isEmpty()) {
            Event event = eventsQueue.poll();  // retrieves and removes first element
            List<Event> newEvents = event.execute();
            eventsQueue.addAll(newEvents);
            History.registerEvent(event);
        }

        History.close();
        Debug.closeAllLogs();
    }

}