package es.urjc.ia.bikesurbanfleets.history;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import es.urjc.ia.bikesurbanfleets.history.entities.HistoricEntity;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Entity;
import es.urjc.ia.bikesurbanfleets.common.util.BoundingBox;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import org.reflections.Reflections;

/**
 * This class finds out the changes which have happened through the entire
 * simulation and registers them.
 *
 * @author IAgroup
 *
 */
public class History {

    private final static String FINAL_GLOBAL_VALUES_FILENAME = "final-global-values.json";
    private final static String SIMULATION_PARAMETERS_FILENAME = "simulation_parameters.json";

    private static int TIMEENTRIES_PER_FILE;
    private static Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .serializeNulls()
            .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
            .setPrettyPrinting()
            .create();
    private static Gson gsonAll = new GsonBuilder()
            .serializeNulls()
            .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
            .setPrettyPrinting()
            .create();

    //**************************************************
    // Method for filtering out certain entities from the history (to reduce history size)
    //
    //
    private static List<Entity> FilterEntitiesForHistory(List<Entity> entities){
        if (entities==null) return null;
        return entities.stream().filter(
                entity -> (EntityHistoricEntityMapping.includeInHistory(entity)) 
        ).collect(Collectors.toList());
    }
    /**
     * It is the current state of the entities in the system.
     */
    private static HistoricEntityCollection activeEntities = new HistoricEntityCollection();

    /**
     * This map stores, for each moment of the simulation, the results of the
     * events in serialized form for writing this information to the history
     * from time to time
     */
    private static TreeMap<Integer, List<EventEntry>> serializedEvents = new TreeMap<>();
    /**
     * This is the path where historic files will be saved.
     */
    private static Path outputPath;

    /**
     * It prepares the history instance to be used. Specifically, it initializes
     * the path of the directory where historic files will be stored.
     *
     * @param historyOutputPath contains the path where the history files will be written
     * 
     * @param TIMEENTRIES_PER_HISTORYFILE numer of events  that will be  be written in each seperate history file
     * 
     * @param boundingBox contains the boundingbox of the simulation
     * @param totalSimulationTime contains the total time (duration) of the simulation in seconds
     * @param initialEntities contains the entities that are initially stored in
     * the history (the entities that are present before the simulation starts)
     */
    public static void init(String historyOutputPath, 
            int TIMEENTRIES_PER_HISTORYFILE, BoundingBox boundingBox, int totalSimulationTime,
            List<Entity> initialEntities, String recomenderParametersString) throws IOException {
        //initialize values
        activeEntities = new HistoricEntityCollection();
        serializedEvents = new TreeMap<Integer, List<EventEntry>>();
        outputPath = Paths.get(historyOutputPath);
        File outputDirectory = new File(outputPath.toString());
        if (outputDirectory.exists() && outputDirectory.isDirectory()) {
            outputDirectory.mkdirs();
        }
        TIMEENTRIES_PER_FILE = TIMEENTRIES_PER_HISTORYFILE;
        //write the global information in a file
        FinalGlobalValues finalGlobalValues = new FinalGlobalValues(boundingBox,totalSimulationTime);
        writeJson(FINAL_GLOBAL_VALUES_FILENAME, finalGlobalValues, gsonAll);
        writeSimulationParameters(SIMULATION_PARAMETERS_FILENAME, recomenderParametersString, gsonAll); 
        
        //add the initialentities to the active entities 
        for (Entity entity : FilterEntitiesForHistory(initialEntities)) {
            if (activeEntities.addToMapFor(instantiateHistoric(entity)) != null) { //there was already an entity
                throw new RuntimeException("illegal program situation: addToActiveEntities");
            }

        }
        //write the initial entities and the prototypes of all historic entities to a file
        //to do maybe not working
        Set<Class<? extends HistoricEntity>> historicEntitiesClasses = new Reflections().getSubTypesOf(HistoricEntity.class);
        for (Class<? extends HistoricEntity> histent : historicEntitiesClasses) {
            String jsonIdentifier = getJsonIdentifier(histent);
            EntitiesJson entjason = new EntitiesJson(histent, activeEntities.getCollectionFor(histent));
            writeJson("entities/" + jsonIdentifier + ".json", entjason, gson);
        }
    }
    /**
     * It creates a file and writes the specified information inside it.
     *
     * @param name It is the name of the file which is created.
     * @param content It is the information which is written in the file.
     */
    private static void writeSimulationParameters(String name, String content, Gson gs) throws IOException {
        // it creates a file with the specified name in the history directory
        File json = outputPath.resolve(name).toFile();
        json.getParentFile().mkdirs();

        // it writes the specified content in the created file
        FileWriter writer = new FileWriter(json);
        writer.write(content);
        writer.close();
    }

    /**
     * It creates a file and writes the specified information inside it.
     *
     * @param name It is the name of the file which is created.
     * @param content It is the information which is written in the file.
     */
    private static void writeJson(String name, Object content, Gson gs) throws IOException {
        // it creates a file with the specified name in the history directory
        File json = outputPath.resolve(name).toFile();
        json.getParentFile().mkdirs();

        // it writes the specified content in the created file
        FileWriter writer = new FileWriter(json);
        gs.toJson(content, writer);
        writer.close();
    }

    /**
     * It saves in a file the initial states of all entities in the system and,
     * in other files, the changes that the entities have been passing throught
     * the entire simulation.
     */
    public static void close() throws IOException {
        writeTimeEntries();
        activeEntities = null;
        serializedEvents = null;
    }

    /**
     * It creates an event entry (in the map of serialized events) to register
     * all the changes detected in the entities involved in the event and saves
     * its entities into the entity collection of updated entities.
     *
     * @param event It is the event to register.
     */
    public static void registerEvent(Event event, int instant, int order) throws Exception {

         //get the referneces of the involved entities
        List<IdReference> involved = getReferencesInvolvedEntities(FilterEntitiesForHistory(event.getInvolvedEntities()));

        // Get the new entities and add them to the active entities.
        Map<String, List<JsonObject>> serializedNewEntities = treatNewEntities(FilterEntitiesForHistory(event.getNewEntities()));

        // get the list of changes of the involved entities
        Map<String, List<JsonObject>> changes = treatInvolvedEntities(FilterEntitiesForHistory(event.getInvolvedEntities()));


        // get the list of old entities
        Map<String, List<JsonObject>> serializedOldEntities = treatOldEntities(FilterEntitiesForHistory(event.getOldEntities()));

        /* If the map of serialized changes doesn't conatin an entry with the time
         * instance of this event, it creates it.
         */
        if (!serializedEvents.containsKey(instant)) {
            // TODO: test entry limit with more real world examples to not generate too large jsons
            if (serializedEvents.size() == TIMEENTRIES_PER_FILE) {
                writeTimeEntries();
            }
            serializedEvents.put(instant, new ArrayList<>());
        }
        // It adds the event to the current time instant
 
        serializedEvents.get(instant).add(
                new EventEntry(event.getClass().getSimpleName(), order, event.getResult(),involved,changes, serializedNewEntities, serializedOldEntities));
    }

    private static List<IdReference> getReferencesInvolvedEntities(List<Entity> invEntities) {
        /* Get the new entities and add them to the active entities.
         */
        if (invEntities == null || invEntities.isEmpty()) {
            return null;
        }
        ArrayList<IdReference> result = new ArrayList<>();
        for (Entity entity : invEntities) {
            Class<? extends HistoricEntity> historicClass = EntityHistoricEntityMapping.getHistoricEntityClass(entity);
            result.add(new IdReference(historicClass, entity.getId()));
        }
        if (result.isEmpty()) return null;
        else return result;
    }

    private static Map<String, List<JsonObject>> treatNewEntities(List<Entity> newEntities) {
        /* Get the new entities and add them to the active entities.
         */
        if (newEntities == null || newEntities.size() == 0) {
            return null;
        }
        Map<String, List<JsonObject>> result = new HashMap<>();
        for (Entity entity : newEntities) {
            //convert to historic
            HistoricEntity historicentity = instantiateHistoric(entity);
            //add to active
            if (activeEntities.addToMapFor(historicentity) != null) { //there was already an entity
                throw new RuntimeException("illegal program situation: addToActiveEntities");
            }

            //serialize the historic entity and put into map
            //put in the serialized map
            Class<? extends HistoricEntity> historicClass = historicentity.getClass();
            String jsonIdentifier = getJsonIdentifier(historicClass);
            if (!result.containsKey(jsonIdentifier)) {
                result.put(jsonIdentifier, new ArrayList<>());
            }
            result.get(jsonIdentifier).add((JsonObject) gson.toJsonTree(historicentity));
        }
        return result;
    }

    /**
     * It detects if any involved entity has changed with regard to its previous
     * state.
     *
     * @param entities It is a list of entities instances.
     * @return a map whose key is the historic entity name and whose value is a
     * Json object which contains the changes.
     */
    private static Map<String, List<JsonObject>> treatInvolvedEntities(List<Entity> entities) throws Exception {
        if (entities == null || entities.size() == 0) {
            return null;
        }
        Map<String, List<JsonObject>> changes = new HashMap<>();

        for (Entity entity : entities) {
            //convert to historic and get old entry
            HistoricEntity newentity = instantiateHistoric(entity);
            HistoricEntity oldEntity = activeEntities.getEntity(newentity);
            if (oldEntity == null) {
                throw new RuntimeException("invalid program state:treatInvolvedEntities");
            } //otherwise everything is fine

            //look for at all possible changes
            String jsonIdentifier = getJsonIdentifier(newentity.getClass());
            JsonObject jsonEntity = new JsonObject();
            // The field type represents an attribute  
            for (Field field : newentity.getClass().getDeclaredFields()) {
                if (field.getName().equals("id")) {
                    continue;
                }
                JsonElement property;
                // changes the attribute visibility to public
                field.setAccessible(true);
                property = propertyChange(field.get(oldEntity), field.get(newentity));
                if (property != null) {
                    jsonEntity.add(field.getName(), property);
                }
            }

            //if there have been any changes
            if (!jsonEntity.entrySet().isEmpty()) {
                //add them to the changes
                if (!changes.containsKey(jsonIdentifier)) {
                    changes.put(jsonIdentifier, new ArrayList<>());
                }
                jsonEntity.add("id", new JsonPrimitive(entity.getId()));
                changes.get(jsonIdentifier).add(jsonEntity);
                //substitute the old value with the new one in the active entities
                if (activeEntities.addToMapFor(newentity) == null) { //there was no  entity before
                    throw new RuntimeException("illegal program situation: addToActiveEntities");
                }
            }
        }
        if (changes.size() == 0) {
            return null;
        }
        return changes;
    }

    private static Map<String, List<JsonObject>> treatOldEntities(List<Entity> oldEntities) {
        if (oldEntities == null || oldEntities.size() == 0) {
            return null;
        }
        Map<String, List<JsonObject>> result = new HashMap<>();
        for (Entity entity : oldEntities) {
            //convert to historic
            HistoricEntity historicentity = instantiateHistoric(entity);
            //remove entity from active entities
            boolean removed = activeEntities.removeFrom(historicentity);
            if (!removed) {
                throw new RuntimeException("invalid program state:treatInvolvedEntities");
            } //otherwise everything is fine

            //serialize the historic entity and put into map
            //put in the serialized map
            Class<? extends HistoricEntity> historicClass = historicentity.getClass();
            String jsonIdentifier = getJsonIdentifier(historicClass);
            if (!result.containsKey(jsonIdentifier)) {
                result.put(jsonIdentifier, new ArrayList<>());
            }
            result.get(jsonIdentifier).add((JsonObject) gson.toJsonTree(historicentity));
        }
        return result;
    }

    /**
     * It creates a Json object which contains the previous and current states
     * of specific property..
     *
     * @param oldProperty It is the previous state of the property.
     * @param newProperty It is the current state of the property.
     * @return a Json object which conatins the previous and curent states of
     * the property.
     */
    private static JsonObject propertyChange(Object oldProperty, Object newProperty) {
        if (Objects.equals(oldProperty, newProperty)) {
            return null;
        }

        JsonObject property = new JsonObject();
        property.add("old", gson.toJsonTree(oldProperty));
        property.add("new", gson.toJsonTree(newProperty));

        return property;
    }

    /**
     * It transforms the map of serialized events into a list of time entries
     * and writes it into a file whose name is set with a format which follows a
     * concrete pattern.
     */
    private static void writeTimeEntries() throws IOException {
        List<TimeEntry> timeEntries = new ArrayList<>();

        serializedEvents.forEach((time, eventEntries) -> {
            timeEntries.add(new TimeEntry(time, eventEntries));
        });
        /*
         * The file name is created with the following format:
         * "first time instant of the simulation-last time instant of the simulation.number of
         * registered time instants of the simulation.json"
         */
        String fileName = new StringBuilder()
                .append(serializedEvents.firstKey()).append("-")
                .append(serializedEvents.lastKey()).append("_")
                .append(serializedEvents.size()).append(".json").toString();

        writeJson(fileName, timeEntries, gson);
        serializedEvents.clear();
    }

    /**
     * It obtains the Json identifier string of a specific historical class.
     *
     * @param historicClass It is the history class whose identifier wants to be
     * found out.
     * @return the string of a Json identifier corresponding to the specified
     * histry class.
     */
    private static String getJsonIdentifier(Class<? extends HistoricEntity> historicClass) {
        JsonIdentifier[] jsonIdentifiers = historicClass.getAnnotationsByType(JsonIdentifier.class);

        if (jsonIdentifiers.length == 0) {
            throw new IllegalStateException("No annotation @JsonIdentifier found for " + historicClass);
        }

        if (jsonIdentifiers.length > 1) {
            throw new IllegalStateException("Found more than one @JsonIdentifier annotation in inheritance chain for " + historicClass);
        }

        return jsonIdentifiers[0].value();
    }

    /**
     * It creates, from a specific entity, the instance of the corresponding
     * historical class.
     *
     * @param entity It is the entity whose history must be created.
     * @return the concrete history corresponding to the entity.
     */
    @SuppressWarnings("unchecked")
    private static HistoricEntity instantiateHistoric(Entity entity) {

        Class<? extends HistoricEntity> historicClass = EntityHistoricEntityMapping.getHistoricEntityClass(entity);

        Class<? extends Entity> constructorParameter = entity.getClass();

        while (!constructorParameter.getSuperclass().equals(Object.class)) {
            constructorParameter = (Class<? extends Entity>) constructorParameter.getSuperclass();
        }

        try {
            Constructor<? extends HistoricEntity> historicConstructor = historicClass.getConstructor(constructorParameter);
            return historicConstructor.newInstance(entity);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new IllegalStateException("No matching constructor found for " + historicClass);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Error trying to instantiate " + historicClass);
        }
    }

    /**
     * This class is used to save the histories of all the entities of the
     * system. It provides methods to save a new entity history and to consult
     * one.
     *
     * @author IAgroup
     *
     */
    private static class HistoricEntityCollection {

        /**
         * It is a map whose key is the class of the historic entity and whose
         * value is another map with the entity identifier as the key and the
         * history of the entity as the value.
         */
        private Map<Class<? extends HistoricEntity>, Map<Integer, HistoricEntity>> entityMaps;

        HistoricEntityCollection() {
            this.entityMaps = new HashMap<>();
        }

        //returns null if there is no such entity
        HistoricEntity getEntity(HistoricEntity he) {
            Class<? extends HistoricEntity> historicClass = he.getClass();
            if (!entityMaps.containsKey(historicClass)) {
                return null;
            }
            return entityMaps.get(historicClass).get(he.getId());
        }

        /**
         *
         * @param entityClass It is the map key.
         * @return a set with all the instances of the given class type and an
         * empty set if there are no instances.
         */
        Collection<HistoricEntity> getCollectionFor(Class<? extends HistoricEntity> entityClass) {
            Map<Integer, HistoricEntity> map = entityMaps.get(entityClass);
            if (map != null) {
                return map.values();
            } else {
                return new ArrayList<>();
            }
        }

        /* It removed the element form the map.
         *
         * @param entity It is the instance to removed from the map.
         * @returns true if the entity existed and was removed and false otherwise
         */
        boolean removeFrom(HistoricEntity entity) {
            Class<? extends HistoricEntity> historicClass = entity.getClass();
            if (!entityMaps.containsKey(historicClass)) {
                return false;
            }
            HistoricEntity e = entityMaps.get(historicClass).remove(entity.getId());
            if (e == null) {
                return false;
            }
            return true;
        }

        /**
         * It adds a new instance of entity historic to the map.
         *
         * @param entityClass It is the key of the amp.
         * @param entity It is the instance to add to the map.
         * @returns the old entry if there was already one element in the
         * collection or null if not
         */
        HistoricEntity addToMapFor(HistoricEntity entity) {
            Class<? extends HistoricEntity> historicClass = entity.getClass();
            if (!entityMaps.containsKey(historicClass)) {
                entityMaps.put(historicClass, new HashMap<>());
            }
            return entityMaps.get(historicClass).put(entity.getId(), entity);
        }
    }

    private static class EntitiesJson {

        @Expose
        private List<String> prototype;

        @Expose
        private Collection<HistoricEntity> instances;

        EntitiesJson(Class<? extends HistoricEntity> historicClass, Collection<HistoricEntity> entities) {
            this.prototype = Arrays.stream(historicClass.getDeclaredFields()).map(Field::getName).collect(Collectors.toList());
            this.instances = entities;
        }
    }

    /**
     * This class represents an event and contains the changes which have
     * occurred with respect to the previus event.
     *
     * @author IAgroup
     */
    private static class EventEntry {

        /**
         * It is the name of the event.
         */
        @Expose
        private String name;

        /**
         * It is the order of the event at the specified time (necessary if
         * there are more than one events at the same time).
         */
        @Expose
        private int order;
        
        @Expose
        private Event.EventResult result;
        
        @Expose
        private Collection<IdReference> involvedEntities;

        @Expose
        private Map<String, List<JsonObject>> changes;
        @Expose
        private Map<String, List<JsonObject>> newEntities;
        @Expose
        private Map<String, List<JsonObject>> oldEntities;

        EventEntry(String name, int order, Event.EventResult res, Collection<IdReference> involved, Map<String, List<JsonObject>> changes, 
                Map<String, List<JsonObject>> newEntities, Map<String, List<JsonObject>> oldEntities) {
            this.name = name;
            this.order = order;
            this.changes = changes;
            this.newEntities = newEntities;
            this.oldEntities = oldEntities;
            this.result=res;
            this.involvedEntities=involved;
        }
    }

    /**
     * This class rpresents a time instant of the simulation and contains all
     * the events which happen at this moment.
     *
     * @author IAgroup
     *
     */
    private static class TimeEntry {

        /**
         * It is the moment when the events happen.
         */
        @Expose
        private int time;

        /**
         * They are the vents which happen at the specific moment. if the time
         * is the same the orden of the events is by increasing order
         */
        @Expose
        private List<EventEntry> events;

        TimeEntry(int time, List<EventEntry> events) {
            this.time = time;
            this.events = events;
        }
    }

    public static class IdReference {

        @Expose
        private String type;

        @Expose
        private Object id;

        private IdReference(Class<? extends HistoricEntity> type, Object id) {
            this.type = getJsonIdentifier(type);
            this.id = id;
        }

        public IdReference(Class<? extends HistoricEntity> type, Integer id) {
            this(type, (Object) id);
        }

        public IdReference(Class<? extends HistoricEntity> type, List<Integer> idList) {
            this(type, (Object) idList);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            IdReference that = (IdReference) o;
            return Objects.equals(type, that.type)
                    && Objects.equals(id, that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, id);
        }
    }

    /**
     * This class represents all the final global values that has to be stored
     * in the history
     */
    private static class FinalGlobalValues {

        /**
         * Total simulation time
         */
        private int totalTimeSimulation;

        /**
         * Bounding Box where the simulation has been reproduced
         */
        private BoundingBox boundingBox;

        public FinalGlobalValues(BoundingBox boundingBox, int totalTimeSimulation) {
            this.totalTimeSimulation = totalTimeSimulation;
            this.boundingBox = boundingBox;
        }
    }
}
