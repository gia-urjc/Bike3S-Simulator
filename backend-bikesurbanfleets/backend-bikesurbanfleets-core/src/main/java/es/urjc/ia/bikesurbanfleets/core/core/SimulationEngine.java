package es.urjc.ia.bikesurbanfleets.core.core;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.common.util.MessageGuiFormatter;
import es.urjc.ia.bikesurbanfleets.core.config.StationsConfig;
import es.urjc.ia.bikesurbanfleets.core.config.UsersConfig;
import es.urjc.ia.bikesurbanfleets.core.UserEvents.EventUserAppears;
import es.urjc.ia.bikesurbanfleets.core.config.GlobalInfo;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Entity;
import es.urjc.ia.bikesurbanfleets.common.util.IdGenerator;
import es.urjc.ia.bikesurbanfleets.common.util.SimpleRandom;
import es.urjc.ia.bikesurbanfleets.history.History;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Bike;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Reservation;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import es.urjc.ia.bikesurbanfleets.common.log.Debug;
import es.urjc.ia.bikesurbanfleets.core.ManagingEvents.EventManaging;
import es.urjc.ia.bikesurbanfleets.core.UserEvents.EventUser;
import es.urjc.ia.bikesurbanfleets.defaultConfiguration.GlobalConfigurationParameters;
import es.urjc.ia.bikesurbanfleets.services.fleetManager.FleetManager;
import es.urjc.ia.bikesurbanfleets.services.graphManager.GraphManager;
import es.urjc.ia.bikesurbanfleets.worldentities.users.User;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * This is the most important class which makes the simulation possible. It uses
 * a queue to manage the events proccessing.
 *
 * @author IAgroup
 *
 */
public final class SimulationEngine {

    /**
     * It allows to compare 2 events by the time instant they'll occur.
     */
    private static Comparator<Event> eventComparatorByTime() {
        return (e1, e2) -> Integer.compare(e1.getInstant(), e2.getInstant());
    }
    private PriorityQueue<EventUser> UserEventsQueue;
    private PriorityQueue<EventManaging> ManagingEventsQueue;
    private int totalUsers;

    /**
     * It creates an event queue where its events are sorted by the time instant
     * when they'll occur.
     */
    public SimulationEngine(GlobalInfo globalInfo, StationsConfig stationsInfo, UsersConfig usersInfo, GraphManager graphManager) throws Exception {

        //******************************************
        //setup everything for doing the simulation
        //******************************************
        //1.   set up the global variables and initialize singleton classes of the simulation
        //this should be done befor setting up the actual entity objects
        Bike.resetIdGenerator();
        Station.resetIdMap();
        User.resetIdGenerator();
        Reservation.resetIdGenerator();
        Debug.init(globalInfo.isDebugMode(), GlobalConfigurationParameters.DEBUG_DIR);
        System.out.println("DEBUG MODE: " + Debug.isDebugmode());
        Reservation.VALID_TIME = globalInfo.getReservationTime();

        //2.   set up stations (with bikes)
        List<Station> stations = setUpStations(stationsInfo);

        //3.   set up general services for the simulation and initiualize them
        SimulationServices services = new SimulationServices(graphManager);
        services.initSimulationServices(globalInfo, stations);
        
        //4. set the simulation date and time
        SimulationDateTime.intSimulationDateTime(globalInfo.getStartDateTime());
 
        //5. get all initial entities and set up the history
        List<Entity> initialentities = new ArrayList<Entity>();
        initialentities.addAll(services.getStationManager().consultBikes());
        initialentities.addAll(services.getStationManager().consultStations());
        initialentities.add(services.getFleetManager());
        History.init(globalInfo.getHistoryOutputPath(), GlobalConfigurationParameters.TIMEENTRIES_PER_HISTORYFILE,
                globalInfo.getBoundingBox(), globalInfo.getTotalSimulationTime(), initialentities, 
                services.getRecommendationSystem().getParameterString());

        //6.   generate the initial events (userappears)
        List<EventUserAppears> userevents=getUserAppearanceEvents(usersInfo, services, globalInfo.getRandomSeed());
        UserEventsQueue = new PriorityQueue<>(userevents.size()+20,eventComparatorByTime());
        UserEventsQueue.addAll(userevents);
        totalUsers=userevents.size();

        //7.   if a fleetmanager is available, call its initialisation 
        ManagingEventsQueue = new PriorityQueue<>(10,eventComparatorByTime());
        FleetManager fleetManager=services.getFleetManager();
        List<EventManaging> initManagingEvents=fleetManager.initialActions();
        if (initManagingEvents != null) {
            ManagingEventsQueue.addAll(initManagingEvents);
        }
        
        //8.
        //******************************************
        //do simulation
        //******************************************
        this.run(globalInfo.getTotalSimulationTime());

        //9.
        //******************************************
        //close everything afterwards
        //******************************************
        Debug.close();
        History.close();
    }

    static private List<Station> setUpStations(StationsConfig stationsInfo) {
        List<Station> stations = new ArrayList<>();
        Gson gson = new Gson();
        stationsInfo.getStations().forEach((s) -> {
            stations.add(gson.fromJson(s, Station.class));
        });
        return stations;
    }

    static private List<EventUserAppears> getUserAppearanceEvents(UsersConfig usersInfo, SimulationServices services, long globalrandomseed) {
        List<EventUserAppears> eventUserAppearsList = new ArrayList<>();
        UserFactory userFactory = new UserFactory();
        IdGenerator idusers = new IdGenerator();

        SimpleRandom simprand = new SimpleRandom(globalrandomseed);
        for (JsonObject userdef : usersInfo.getUsers()) {
            int seed = simprand.nextInt();
            User user = userFactory.createUser(userdef, services, seed);
            int instant = user.getAppearanceInstant();
            GeoPoint position = user.getPosition();
            // Is necessary to have the user position initialized to null to write changes.
            // Position is asigned again in EventUserAppears
            user.setPosition(null);
            eventUserAppearsList.add(new EventUserAppears(instant, user, position));
        }
        return eventUserAppearsList;
    }

    public synchronized void run(int simulationtime) throws Exception {

        // Those variables are used to control de percentage of the simulation done
        double percentage = 0D;
        double oldpercentagepresented = 0D;
        int lastInstant = 0;
        int order = 0;
        Event currentEvent=null;

        MessageGuiFormatter.showPercentageForGui(percentage);
        try {
            while (!UserEventsQueue.isEmpty() || !ManagingEventsQueue.isEmpty()) {
                
                //get the earliest event to execute
                Event userevent = UserEventsQueue.peek();  // retrieves but does not remove first elements
                Event managementevent=ManagingEventsQueue.peek();
                if (userevent==null) currentEvent=ManagingEventsQueue.poll();
                else if (managementevent==null) currentEvent=UserEventsQueue.poll();
                else if (managementevent.getInstant()<=userevent.getInstant()) currentEvent=ManagingEventsQueue.poll();
                else currentEvent=UserEventsQueue.poll();

                //check the time
                if (currentEvent.getInstant()< lastInstant) {
                    throw new RuntimeException("Illegal event execution");
                }
                if (currentEvent.getInstant()>= simulationtime) {
                    System.out.println("Finished because out of ending simulation time.");
                    break; //goes out of the loop
                }
                
                //The event has to be processed
                //set the current simulation date and instant
                SimulationDateTime.setCurrentSimulationInstant(currentEvent.getInstant());
                //now process the event
                if (currentEvent instanceof EventUser) {
                    EventUser newEvent = ((EventUser)currentEvent).execute();
                    if (newEvent != null) {
                        UserEventsQueue.add(newEvent);
                    }
                } else {//if it is managingevent
                    List<EventManaging> newEvents = ((EventManaging)currentEvent).execute();
                    if (newEvents != null) {
                        ManagingEventsQueue.addAll(newEvents);
                    }
                }

                //put event on output if debug
                if (Debug.isDebugmode()) {
                    System.out.println(currentEvent.toString());
                }

                //reguister the event in the history
                
                if (currentEvent.getInstant() > lastInstant) {
                    order = 0;
                } else {
                    order++;
                }
                History.registerEvent(currentEvent, currentEvent.getInstant(), order);
                
                // Shows the actual percentage in the stdout for frontend
                if (currentEvent.getClass().getSimpleName().equals(EventUserAppears.class.getSimpleName())) {
                    //show only every 5 percent
                    percentage += (((double) 1 / (double) totalUsers) * 100);
                    if (percentage >= oldpercentagepresented + 5D) {
                        MessageGuiFormatter.showPercentageForGui(percentage);
                        oldpercentagepresented = percentage;
                    }
                }
                lastInstant = currentEvent.getInstant();
            }
            MessageGuiFormatter.showPercentageForGui(100D);
        } catch (Exception e) {
            exceptionTreatment(e, currentEvent);
        }

    }

        private void exceptionTreatment(Exception e, Event ev) {
        MessageGuiFormatter.showErrorsForGui(e);
        System.out.println(ev.toString());
        throw new RuntimeException(e.getMessage());
    }

}
