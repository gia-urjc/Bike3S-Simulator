package com.urjc.iagroup.bikesurbanfloats.history;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.urjc.iagroup.bikesurbanfloats.config.SimulationConfiguration;
import com.urjc.iagroup.bikesurbanfloats.core.SystemManager;
import com.urjc.iagroup.bikesurbanfloats.entities.Entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

public class History {

    static Gson gson = new Gson();

    private static ChangeEntry lastEntry;
    private static ChangeEntry nextEntry;

    private static ArrayList<JsonObject> serializedEntries = new ArrayList<>();

    private static Set<Class<? extends HistoricEntity>> historicClasses = new HashSet<>();

    public static void init(SimulationConfiguration simulationConfiguration, SystemManager systemManager) {
        lastEntry = new ChangeEntry(0);
        nextEntry = new ChangeEntry(0);

        // TODO: write file
    }

    public static void close() {}

    public static void registerNewEntity(Entity entity) {
        Class<? extends Entity> entityClass = entity.getClass();
        Class<? extends HistoricEntity> historicClass = getReferenceClass(entityClass);
        nextEntry.addToMapFor(historicClass, instantiateHistoric(entity));
        // TODO: add to initial file
    }

    public static void registerForChange(int timeInstant, Entity entity) {
        if (timeInstant > nextEntry.getTimeInstant()) {
            JsonObject changes = serializeChanges();

            if (changes != null) {
                serializedEntries.add(changes);
            }

            // TODO: write serializedEntries to file and clear list

            lastEntry = nextEntry;

            nextEntry = new ChangeEntry(timeInstant);
        }

        Class<? extends HistoricEntity> historicClass = getReferenceClass(entity.getClass());
        historicClasses.add(historicClass);
        nextEntry.addToMapFor(historicClass, instantiateHistoric(entity));
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

}
