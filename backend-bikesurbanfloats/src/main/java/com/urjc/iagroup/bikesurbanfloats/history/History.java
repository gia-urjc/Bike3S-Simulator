package com.urjc.iagroup.bikesurbanfloats.history;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import com.urjc.iagroup.bikesurbanfloats.config.SimulationConfiguration;
import com.urjc.iagroup.bikesurbanfloats.entities.Entity;
import com.urjc.iagroup.bikesurbanfloats.events.Event;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * This class finds out the changes which have happened through the entire simulation and registers them.
 *
 * @author IAgroup
 */
public class History {

    private static Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .serializeNulls()
            .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
            .setPrettyPrinting()
            .create();

    /**
     * It is the initial states of the entities in the system.
     */
    private static EntityCollection initialEntities = new EntityCollection();

    /**
     * It is the current state of the entities in the system.
     */
    private static EntityCollection updatedEntities = new EntityCollection();

    /**
     * This map stores, for each momento of the simulation, the changes of the entities of
     * all the events ocurred in a specific time instant.
     */
    private static TreeMap<Integer, List<EventEntry>> serializedEvents = new TreeMap<>();

    private static Path outputPath = Paths.get("history");

    public static void init(SimulationConfiguration simulationConfiguration) {
        /*initialEntities = new EntityCollection();
        updatedEntities = new EntityCollection();*/

        // TODO: save history output path in the indicated path in configuration file
    }

    /**
     * It saves in a file the initial states of all entities in the system and, in other,
     * the changes that the entities have been suffering throught the entire simulation.
     */
    public static void close() throws IOException {

        // TODO: maybe split entities.json into multiple files, e.g. entities/users.json

    	/*
    	 * It is a map with the names of the entities'  history classes as the key and
    	 * a list of history classes of a concrete entity as the vallue.
    	 */
        Map<String, Collection<HistoricEntity>> entries = new HashMap<>();
        initialEntities.getEntityMaps().forEach((historicClass, entities) -> {
            String jsonIdentifier = getJsonIdentifier(historicClass);
            entries.put(jsonIdentifier, entities.values());
        });

        writeJson("entities.json", entries);
        writeTimeEntries();

        // TODO: copy configuration file
    }

    /**
     * It registers a new entity in the map of initial entities and in the map of updated entities.
     *
     * @param entity It is the entity to register.
     */

    public static void registerEntity(Entity entity) {
        Class<? extends HistoricEntity> historicClass = getReferenceClass(entity.getClass());
        HistoricEntity historicEntity = instantiateHistoric(entity);
        initialEntities.addToMapFor(historicClass, historicEntity);
        updatedEntities.addToMapFor(historicClass, historicEntity);
    }

    /**
     * It creates an event entry (in the map of serialized events) to register all the
     * changes detected in the entities involved in the event and creates and saves
     * the corresponding created historical entities into the entity collection of updated entities.
     *
     * @param event It is the event to register.
     */
    public static void registerEvent(Event event) throws IOException {
        
        /* It creates the historical entities instances coreesponding to all the 
         * entities involved in an evet. 
         */
        List<HistoricEntity> historicEntities = new ArrayList<>();
        for (Entity entity : event.getEntities()) {
            historicEntities.add(instantiateHistoric(entity));
        }
        
        /* It obtains the changes that the created historical entities have suffered 
         * with respect to previous ones. 
         */
        Map<String, List<JsonObject>> changes = serializeChanges(historicEntities);
        
        /* If the map of serialized changes doesn't conatin an entry with the time 
         * instance of this event, it creates it.
         */
        if (!serializedEvents.containsKey(event.getInstant())) {
        	/* A file which contains serialized events can only save 100 time instants
        	 * So, if the map of serialized events already contains 100 entries, it muts be written in a file  
        	 */
            if (serializedEvents.size() == 100) {
                writeTimeEntries();
                serializedEvents.clear();
            }

            serializedEvents.put(event.getInstant(), new ArrayList<>());
        }

        // It creates a new event entry and adds it to the map of serialized events
        serializedEvents.get(event.getInstant()).add(new EventEntry(event.getClass().getSimpleName(), changes));
        
        /* It adds all the historical entities created from the event to the entity 
         * collection of updated entities 
         */
        for (HistoricEntity entity : historicEntities) {
            updatedEntities.addToMapFor(entity.getClass(), entity);
        }
    }

    /**
     * It creates a file and writes the specified information inside it.
     *
     * @param name    It is the name of the file which is created.
     * @param content It is the information which is written in the file.
     */
    private static void writeJson(String name, Object content) throws IOException {
        // it creates a file with the specified name in the history directory
        File json = outputPath.resolve(name).toFile();
        json.getParentFile().mkdirs();

        // it writes the specified content in the created file
        try (FileWriter writer = new FileWriter(json)) {
            gson.toJson(content, writer);
        }
    }

    /**
     * It transforms the map of serialized events into a list of time entries and writes it
     * into a file whose name is created with a format which follows a concrete pattern.
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

        writeJson(fileName, timeEntries);
    }

    /**
     * FALTA:
     *
     * @param entities
     * @return
     */
    private static Map<String, List<JsonObject>> serializeChanges(List<HistoricEntity> entities) {
        Map<String, List<JsonObject>> changes = new HashMap<>();

        for (HistoricEntity entity : entities) {
            Class<? extends HistoricEntity> historicClass = entity.getClass();
            HistoricEntity oldEntity = updatedEntities.getMapFor(historicClass).get(entity.getId());
            String jsonIdentifier = getJsonIdentifier(historicClass);
            JsonObject jsonEntity = new JsonObject();

            // TODO: maybe read private fields instead of methods for consistency with gson
            for (Field field : historicClass.getDeclaredFields()) {
                if (field.getName().equals("id")) continue;

                JsonElement property;

                field.setAccessible(true);

                try {
                    property = propertyChange(field.get(oldEntity), field.get(entity));
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new IllegalStateException("Error reading field " + field + " from " + historicClass);
                }

                if (property != null) {
                    jsonEntity.add(field.getName(), property);
                }
            }

            if (!jsonEntity.entrySet().isEmpty()) {
                jsonEntity.add("id", new JsonPrimitive(entity.getId()));

                if (!changes.containsKey(jsonIdentifier)) {
                    changes.put(jsonIdentifier, new ArrayList<>());
                }

                changes.get(jsonIdentifier).add(jsonEntity);
            }
        }

        return changes;
    }

    /**
     * It finds out, from an entity class, the corresponding historical class.
     *
     * @param entityClass It is the entity class whose corresponding history class musts be found out.
     * @return the corresponding history class to the entity class.
     */
    private static Class<? extends HistoricEntity> getReferenceClass(Class<? extends Entity> entityClass) {
        HistoryReference[] referenceClasses = entityClass.getAnnotationsByType(HistoryReference.class);

        if (referenceClasses.length == 0) {
            throw new IllegalStateException("No annotation @HistoryReference found for " + entityClass);
        }

        if (referenceClasses.length > 1) {
            throw new IllegalStateException("Found more than one @HistoryReference annotation in inheritance chain for " + entityClass);
        }

        return referenceClasses[0].value();
    }

    /**
     * It obtains the Json identifier string of a specific historical class.
     *
     * @param historicClass It is the history class whose identifier wants to be found out.
     * @return the string of a Json identifier corresponding to the specified histry class.
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
     * It creates, from a specific entity, the instance of the corresponding historical class.
     *
     * @param entity It is the entity whose history must be created.
     * @return the concrete history corresponding to the entity.
     */
    @SuppressWarnings("unchecked")
    private static HistoricEntity instantiateHistoric(Entity entity) {

        Class<? extends Entity> entityClass = entity.getClass();
        Class<? extends HistoricEntity> historicClass = getReferenceClass(entityClass);

        Class<? extends Entity> constructorParameter = entityClass;

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
     * It creates a Json object which contains the previous and current states of specific property..
     *
     * @param oldProperty It is the previous state of the property.
     * @param newProperty It is the current state of the property.
     * @return a Json object which conatins the previous and curent states of the property.
     */
    private static JsonObject propertyChange(Object oldProperty, Object newProperty) {
        if (Objects.equals(oldProperty, newProperty)) return null;

        JsonObject property = new JsonObject();
        property.add("old", gson.toJsonTree(oldProperty));
        property.add("new", gson.toJsonTree(newProperty));

        return property;
    }


    /**
     * This class is used to save the histories of all the entities of the system.
     * It provides methods to save a new entity history and to consult one.
     *
     * @author IAgroup
     */
    private static class EntityCollection {
        /**
         * It is a map whose key is the class of the historic entity and whose value is another map
         * with the entity identifier as the key and the history of the entity as the value.
         */
        private Map<Class<? extends HistoricEntity>, Map<Integer, HistoricEntity>> entityMaps;

        EntityCollection() {
            this.entityMaps = new HashMap<>();
        }

        Map<Class<? extends HistoricEntity>, Map<Integer, HistoricEntity>> getEntityMaps() {
            return entityMaps;
        }

        /**
         * @param entityClass It is the map key.
         * @return a map with all the instances of the given class type.
         */
        Map<Integer, HistoricEntity> getMapFor(Class<? extends HistoricEntity> entityClass) {
            return entityMaps.get(entityClass);
        }

        /**
         * It adds a new instance of entity historic to the map.
         *
         * @param entityClass It is the key of the amp.
         * @param entity      It is the instance to add to the map.
         */
        void addToMapFor(Class<? extends HistoricEntity> entityClass, HistoricEntity entity) {
            if (!entityMaps.containsKey(entityClass)) {
                entityMaps.put(entityClass, new HashMap<>());
            }
            entityMaps.get(entityClass).put(entity.getId(), entity);
        }
    }

    /**
     * This class represents an event and contains the changes which have occurred
     * with respect to the previus event.
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
         * They are the differences between an event and the next one.
         */
        @Expose
        private Map<String, List<JsonObject>> changes;

        EventEntry(String name, Map<String, List<JsonObject>> changes) {
            this.name = name;
            this.changes = changes;
        }
    }

    /**
     * This class rpresents a time instant of the simulation and contains all the events
     * which happen at this moment.
     *
     * @author IAgroup
     */
    private static class TimeEntry {
        /**
         * It is the moment when the events happen.
         */
        @Expose
        private int time;

        /**
         * They are the vents which happen at the specific moment.
         */
        @Expose
        private List<EventEntry> events;

        TimeEntry(int time, List<EventEntry> events) {
            this.time = time;
            this.events = events;
        }
    }

}
