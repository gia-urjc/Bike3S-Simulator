package es.urjc.ia.bikesurbanfleets.core.core;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.services.SimulationServiceConfigData;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.common.util.MessageGuiFormatter;
import es.urjc.ia.bikesurbanfleets.core.config.StationsConfig;
import es.urjc.ia.bikesurbanfleets.core.config.UsersConfig;
import es.urjc.ia.bikesurbanfleets.core.events.EventUserAppears;
import es.urjc.ia.bikesurbanfleets.common.config.GlobalInfo;
import es.urjc.ia.bikesurbanfleets.common.util.IdGenerator;
import es.urjc.ia.bikesurbanfleets.common.util.SimpleRandom;
import es.urjc.ia.bikesurbanfleets.history.FinalGlobalValues;
import es.urjc.ia.bikesurbanfleets.history.History;
import es.urjc.ia.bikesurbanfleets.history.entities.HistoricReservation;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Reservation;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.log.Debug;
import es.urjc.ia.bikesurbanfleets.users.User;
import es.urjc.ia.bikesurbanfleets.users.UserFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * This is the most important class which makes the simulation possible. It uses
 * a queue to manage the events proccessing.
 *
 * @author IAgroup
 *
 */
public class SimulationEngine {

    private PriorityQueue<Event> eventsQueue = new PriorityQueue<>();
    private GlobalInfo globalInfo;
    private UsersConfig usersInfo;
    private List<Station> stations;

    /**
     * It creates an event queue where its events are sorted by the time instant
     * when they'll occur.
     */
    public SimulationEngine(GlobalInfo globalInfo, StationsConfig stationsInfo, UsersConfig usersInfo,
            String mapDir) throws Exception {
        this.globalInfo = globalInfo;
        this.usersInfo = usersInfo;
        this.stations = stationsInfo.getStations();

        // ----
        // TODO make it flexible to different properties
        JsonObject graphParameters = new JsonObject();
        graphParameters.addProperty("mapDir", mapDir);

        SimulationServiceConfigData servicesConfigData = new SimulationServiceConfigData();
        servicesConfigData.setBbox(globalInfo.getBoundingBox())
                .setGraphManagerType(globalInfo.getGraphManagerType())
                .setGraphParameters(graphParameters)
                .setRecomSystemType(globalInfo.getRecommendationSystemTypeJsonDescription())
                .setStations(stationsInfo.getStations());

        SimulationServices services = new SimulationServices(servicesConfigData);

        this.eventsQueue = new PriorityQueue<>(processUsers(services));
        Reservation.VALID_TIME = globalInfo.getReservationTime();

        //needed if there's no reservations in the system
        History.reservationClass(HistoricReservation.class);
    }

    private List<EventUserAppears> processUsers(SimulationServices services) {
        List<EventUserAppears> eventUserAppearsList = new ArrayList<>();
        UserFactory userFactory = new UserFactory();
        IdGenerator idusers = new IdGenerator();

        SimpleRandom simprand = new SimpleRandom(globalInfo.getRandomSeed());
        for (JsonObject userdef : usersInfo.getUsers()) {
            int seed = simprand.nextInt();
            User user = userFactory.createUser(userdef, services, seed);
            int instant = user.getInstant();
            GeoPoint position = user.getPosition();
            // Is necessary to have the user position initialized to null to write changes.
            // Position is asigned again in EventUserAppears
            user.setPosition(null);
            eventUserAppearsList.add(new EventUserAppears(instant, user, position));
        }

        return eventUserAppearsList;
    }

    public void run() throws Exception {

        // Those variables are used to control de percentage of the simulation done
        int totalUsers = eventsQueue.size();
        double percentage = 0;
        double oldpercentagepresented = 0;
        int lastInstant = 0;

        MessageGuiFormatter.showPercentageForGui(percentage);

        while (!eventsQueue.isEmpty()) {
            Event event = eventsQueue.poll();  // retrieves and removes first element

            //check if the instant is after the last one
            if (event.getInstant() < lastInstant) {
                throw new RuntimeException("Illegal event execution");
            }
            lastInstant = event.getInstant();

            // Shows the actual percentage in the stdout for frontend
            if (event.getClass().getSimpleName().equals(EventUserAppears.class.getSimpleName())) {
                //show only every 5 percent
                percentage += (((double) 1 / (double) totalUsers) * 100);
                if (percentage >= oldpercentagepresented + 5D) {
                    MessageGuiFormatter.showPercentageForGui(percentage);
                    oldpercentagepresented = percentage;
                }
            }

            if (Debug.isDebugmode()) {
                System.out.println(event.toString());
            }

            List<Event> newEvents = event.execute();
            eventsQueue.addAll(newEvents);
            History.registerEvent(event);

            // if it is the last event, save the global values of the simulation
            if (eventsQueue.isEmpty()) {
                FinalGlobalValues finalGlobalValues = new FinalGlobalValues();
                finalGlobalValues.setTotalTimeSimulation(this.globalInfo.getTotalSimulationTime());
                finalGlobalValues.setBoundingBox(this.globalInfo.getBoundingBox());
                History.writeGlobalInformation(finalGlobalValues);
            }
        }
        MessageGuiFormatter.showPercentageForGui(100D);
    }

}
