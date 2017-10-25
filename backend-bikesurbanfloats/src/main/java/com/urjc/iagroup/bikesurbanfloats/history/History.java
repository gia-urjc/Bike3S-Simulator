package com.urjc.iagroup.bikesurbanfloats.history;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.urjc.iagroup.bikesurbanfloats.config.SimulationConfiguration;
import com.urjc.iagroup.bikesurbanfloats.entities.Entity;
import com.urjc.iagroup.bikesurbanfloats.entities.User;
import com.urjc.iagroup.bikesurbanfloats.entities.models.UserModel;
import com.urjc.iagroup.bikesurbanfloats.events.EventUserAppears;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class History {

    static Gson gson = new Gson();

    private static HistoryEntry lastEntry;
    private static HistoryEntry nextEntry;

    private static ArrayList<JsonObject> serializedEntries = new ArrayList<>();

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

        try {
            Class<? extends Entity> entityClass = entity.getClass();
            Class<? extends HistoricEntity> historicClass = EntityMapping.getFor(entityClass).getHistoricClass();
            Constructor<? extends HistoricEntity> historicConstructor = historicClass.getConstructor(entity.getClass());
            nextEntry.addToMapFor(historicClass, historicConstructor.newInstance(entity));
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("A mapping in EntityMapping is probably wrong, or an implementation of HistoricEntity has a wrong copy constructor");
        }
    }

    private static JsonObject serializeChanges() {
        JsonObject entry = new JsonObject();

        for (EntityMapping mapping : EntityMapping.values()) {
            List<JsonObject> subTree = new ArrayList<>();

            for (HistoricEntity entity : nextEntry.getMapFor(mapping.getHistoricClass()).values()) {
                JsonObject changes = entity.makeChangeEntryFrom(lastEntry.getMapFor(mapping.getHistoricClass()).get(entity.getId()));
                if (changes != null) subTree.add(changes);
            }

            if (!subTree.isEmpty()) {
                entry.add(mapping.getJsonIdentifier(), gson.toJsonTree(subTree));
            }
        }

        return entry.entrySet().isEmpty() ? null : entry;
    }

}
