package es.urjc.ia.bikesurbanfleets.core.core;


import com.google.protobuf.Message;
import es.urjc.bikesurbanfleets.services.GraphManagerType;
import es.urjc.bikesurbanfleets.services.RecommendationSystemType;
import es.urjc.bikesurbanfleets.services.SimulationServiceConfigData;
import es.urjc.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.common.util.MessageGuiFormatter;
import es.urjc.ia.bikesurbanfleets.core.config.StationsConfig;
import es.urjc.ia.bikesurbanfleets.core.config.UsersConfig;
import es.urjc.ia.bikesurbanfleets.core.events.EventUserAppears;
import es.urjc.ia.bikesurbanfleets.common.config.GlobalInfo;
import es.urjc.ia.bikesurbanfleets.history.History;
import es.urjc.ia.bikesurbanfleets.history.entities.HistoricReservation;
import es.urjc.ia.bikesurbanfleets.infraestructure.InfraestructureManager;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Reservation;
import es.urjc.ia.bikesurbanfleets.log.Debug;
import es.urjc.ia.bikesurbanfleets.users.User;
import es.urjc.ia.bikesurbanfleets.users.UserFactory;
import es.urjc.ia.bikesurbanfleets.usersgenerator.SingleUser;
import org.apache.commons.math3.util.Precision;

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
    private UsersConfig usersInfo;

    /**
     * It creates an event queue where its events are sorted by the time instant when they'll occur.
     */
    public SimulationEngine(GlobalInfo globalInfo, StationsConfig stationsInfo, UsersConfig usersInfo,
                            String mapPath) throws Exception {
        this.globalInfo = globalInfo;
        this.usersInfo = usersInfo;
        SimulationServiceConfigData servicesConfigData = new SimulationServiceConfigData();
        servicesConfigData.setBbox(globalInfo.getBoundingBox())
            .setGraphManagerType(GraphManagerType.valueOf(globalInfo.getGraphManagerType()))
            .setMapDir(mapPath)
            .setRecomSystemType(RecommendationSystemType.valueOf(globalInfo.getRecommendationSystemType()))
            .setStations(stationsInfo.getStations())
            .setMaxDistance(globalInfo.getMaxDistanceRecommendation());

        SimulationServices services = new SimulationServices(servicesConfigData);

        this.eventsQueue = new PriorityQueue<>(processUsers(services));
        Reservation.VALID_TIME = globalInfo.getReservationTime();
        Debug.DEBUG_MODE = globalInfo.isDebugMode();

        //needed if there's no reservations in the system
        History.reservationClass(HistoricReservation.class);
    }

    private List<EventUserAppears> processUsers(SimulationServices services) {
        List<EventUserAppears> eventUserAppearsList = new ArrayList<>();
        UserFactory userFactory = new UserFactory();
        for (SingleUser singleUser: usersInfo.getUsers()) {
            User user = userFactory.createUser(singleUser.getUserType(), services);
            int instant = singleUser.getTimeInstant();
            GeoPoint position = singleUser.getPosition();
            eventUserAppearsList.add(new EventUserAppears(instant, user, position));
        }

        return eventUserAppearsList;
    }

    public void run() throws Exception {

        History.init(globalInfo.getHistoryOutputPath());
        Debug.init();

        // Those variables are used to control de percentage of the simulation done
        int totalUsers = eventsQueue.size();
        double percentage = 0;

        MessageGuiFormatter.showPercentageForGui(percentage);

        while (!eventsQueue.isEmpty()) {
            Event event = eventsQueue.poll();  // retrieves and removes first element

            if(event.getClass().getSimpleName().equals(EventUserAppears.class.getSimpleName())) {
                percentage += (((double) 1 /(double) totalUsers) * 100);
                MessageGuiFormatter.showPercentageForGui(percentage);
            }

            if(Debug.DEBUG_MODE) {
                System.out.println(event.toString());
            }

            List<Event> newEvents = event.execute();
            eventsQueue.addAll(newEvents);
            History.registerEvent(event);
        }

        History.close();
    }

}