package com.urjc.iagroup.bikesurbanfloats.history;

import com.google.gson.*;
import com.urjc.iagroup.bikesurbanfloats.config.SimulationConfiguration;
import com.urjc.iagroup.bikesurbanfloats.entities.Entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

public class History {

    static Gson gson = new GsonBuilder()
            .serializeNulls()
            .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
            .setPrettyPrinting()
            .create();

    private static ChangeEntry entitiesEntry;
    private static ChangeEntry lastEntry;
    private static ChangeEntry nextEntry;

    private static ArrayList<JsonObject> serializedEntries = new ArrayList<>();

    private static Set<Class<? extends HistoricEntity>> historicClasses = new HashSet<>();

    public static void init(SimulationConfiguration simulationConfiguration) {
        entitiesEntry = new ChangeEntry(0);
        lastEntry = new ChangeEntry(0);
        nextEntry = new ChangeEntry(0);

        // TODO: save history output path
    }

    public static void close() {
        // TODO: write entitiesEntry to file
    }

    public static void registerNewEntity(Entity... entities) {
        for (Entity entity : entities) {
            Class<? extends HistoricEntity> historicClass = getReferenceClass(entity.getClass());
            HistoricEntity historicEntity = instantiateHistoric(entity);
            nextEntry.addToMapFor(historicClass, historicEntity);
            entitiesEntry.addToMapFor(historicClass, historicEntity);
        }
    }

    public static void registerForChange(int timeInstant, Entity... entities) {
        if (timeInstant > nextEntry.getTimeInstant()) {
            JsonObject changes = serializeChanges();

            if (changes != null) {
                serializedEntries.add(changes);
            }

            // TODO: write serializedEntries to file and clear list

            lastEntry = nextEntry;

            nextEntry = new ChangeEntry(timeInstant);
        }

        for (Entity entity : entities) {
            Class<? extends HistoricEntity> historicClass = getReferenceClass(entity.getClass());
            historicClasses.add(historicClass);
            nextEntry.addToMapFor(historicClass, instantiateHistoric(entity));
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
                HistoricEntity oldEntity = lastEntry.getMapFor(historicClass).get(entity.getId());

                JsonObject changes = new JsonObject();

                // TODO: maybe read private fields instead of methods for consistency with gson
                for (Method getter : historicClass.getMethods()) {
                    if (getter.getName().equals("getId")) continue;

                    JsonObject property;

                    if (HistoricEntity.class.isAssignableFrom(getter.getReturnType())) {
                        property = HistoricEntity.idReferenceChange(oldEntity, entity);
                    } else try {
                        property = HistoricEntity.propertyChange(getter.invoke(oldEntity), getter.invoke(entity));
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new IllegalStateException("Error invoking getter on " + historicClass);
                    }

                    if (!property.entrySet().isEmpty()) {
                        changes.add(getFieldName(getter.getName()), property);
                    }
                }

                if (!changes.entrySet().isEmpty()) {
                    changes.add("id", new JsonPrimitive(entity.getId()));
                    subTree.add(changes);
                }
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

    /**
     * Converts the name of a getter method to its field equivalent.
     * For example getAverageWalkingVelocity -> averageWalkingVelocity
     * @param methodName
     * @return
     */
    private static String getFieldName(String methodName) {
        String[] nameParts = methodName.split("(?=\\p{Lu})");
        nameParts = Arrays.copyOfRange(nameParts, 1, nameParts.length);
        nameParts[0] = nameParts[0].toLowerCase();
        return String.join("", nameParts);
    }

}
