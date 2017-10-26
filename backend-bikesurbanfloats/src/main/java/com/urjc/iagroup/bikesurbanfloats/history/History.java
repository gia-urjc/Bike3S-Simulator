package com.urjc.iagroup.bikesurbanfloats.history;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.urjc.iagroup.bikesurbanfloats.config.SimulationConfiguration;
import com.urjc.iagroup.bikesurbanfloats.entities.Entity;
import com.urjc.iagroup.bikesurbanfloats.entities.User;
import com.urjc.iagroup.bikesurbanfloats.entities.models.UserModel;
import com.urjc.iagroup.bikesurbanfloats.events.EventUserAppears;
import com.urjc.iagroup.bikesurbanfloats.history.entities.HistoricBike;
import com.urjc.iagroup.bikesurbanfloats.history.entities.HistoricStation;
import com.urjc.iagroup.bikesurbanfloats.history.entities.HistoricUser;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;

public class History {

    static Gson gson = new Gson();

    private static HistoryEntry lastEntry;
    private static HistoryEntry nextEntry;

    private static ArrayList<JsonObject> serializedEntries = new ArrayList<>();

    private static Set<Class<? extends HistoricEntity>> historicClasses = new HashSet<>();

    public static void init(List<EventUserAppears> userAppearsList, SimulationConfiguration simulationConfiguration) {
        lastEntry = new HistoryEntry(0);
        nextEntry = new HistoryEntry(0);
        nextEntry.getStations().putAll(simulationConfiguration.getStations().stream().collect(Collectors.toMap(Entity::getId, HistoricStation::new)));
        JsonObject entry = new JsonObject();

        List<JsonObject> users = new ArrayList<>();

        for (EventUserAppears event : userAppearsList) {
            JsonObject serializedUser = new JsonObject();
            User user = event.getUser();
            nextEntry.getUsers().put(user.getId(), new HistoricUser(user));

            serializedUser.add("timeInstant", new JsonPrimitive(event.getInstant()));
            serializedUser.add("user", gson.toJsonTree(user, UserModel.class));

            users.add(serializedUser);
        }


        nextEntry.getBikes().putAll(simulationConfiguration.getBikes().stream().collect(Collectors.toMap(Entity::getId, HistoricBike::new)));

        entry.add("userAppearanceEvents", gson.toJsonTree(users));
        entry.add("stations", gson.toJsonTree(simulationConfiguration.getStations()));
        // entry.add("bikes", gson.toJsonTree(simulationConfiguration.bikes));

        // TODO: write file
    }

    public static void register(int timeInstant, Entity entity) {
        if (timeInstant > nextEntry.getTimeInstant()) {
            JsonObject changes = serializeChanges();

            if (changes != null) {
                serializedEntries.add(changes);
            }

            // TODO: write serializedEntries to file and clear list

            lastEntry = nextEntry;

            nextEntry = new HistoryEntry(timeInstant);
        }

        Class<? extends Entity> entityClass = entity.getClass();
        HistoryReference[] referenceClasses = entityClass.getAnnotationsByType(HistoryReference.class);

        if (referenceClasses.length == 0) {
            throw new IllegalStateException("No annotation @HistoryReference found for " + entityClass);
        }

        if (referenceClasses.length > 1) {
            throw new IllegalStateException("Found more than one @HistoryReference annotation in inheritance chain for " + entityClass);
        }

        Class<? extends HistoricEntity> historicClass = referenceClasses[0].value();

        historicClasses.add(historicClass);

        try {
            Constructor<? extends HistoricEntity> historicConstructor = historicClass.getConstructor(entityClass);
            nextEntry.addToMapFor(historicClass, historicConstructor.newInstance(entity));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new IllegalStateException("No matching constructor found for " + historicClass);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Error trying to instantiate " + historicClass);
        }
    }

    private static JsonObject serializeChanges() {
        JsonObject entry = new JsonObject();

        for (Class<? extends HistoricEntity> historicClass : historicClasses) {
            List<JsonObject> subTree = new ArrayList<>();

            JsonIdentifier[] jsonIdentifiers = historicClass.getAnnotationsByType(JsonIdentifier.class);

            if (jsonIdentifiers.length == 0) {
                throw new IllegalStateException("No annotation @JsonIdentifier found for " + historicClass);
            }

            if (jsonIdentifiers.length > 1) {
                throw new IllegalStateException("Found more than one @JsonIdentifier annotation in inheritance chain for " + historicClass);
            }

            for (HistoricEntity entity : nextEntry.getMapFor(historicClass).values()) {
                JsonObject changes = entity.makeChangeEntryFrom(lastEntry.getMapFor(historicClass).get(entity.getId()));
                if (changes != null) subTree.add(changes);
            }

            if (!subTree.isEmpty()) {
                entry.add(jsonIdentifiers[0].value(), gson.toJsonTree(subTree));
            }
        }

        return entry.entrySet().isEmpty() ? null : entry;
    }

}
