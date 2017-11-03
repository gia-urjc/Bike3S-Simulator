package com.urjc.iagroup.bikesurbanfloats.history;

import com.google.gson.*;
import com.urjc.iagroup.bikesurbanfloats.config.SimulationConfiguration;
import com.urjc.iagroup.bikesurbanfloats.entities.Entity;
import com.urjc.iagroup.bikesurbanfloats.events.Event;

import javax.validation.constraints.NotNull;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

public class History {

    private static Gson gson = new GsonBuilder()
            .serializeNulls()
            .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
            .setPrettyPrinting()
            .create();

    private static EntityCollection initialEntities;
    private static EntityCollection updatedEntities;

    private static Map<Integer, List<JsonObject>> serializedEntries = new HashMap<>();

    private static Set<Class<? extends HistoricEntity>> historicClasses = new HashSet<>();

    public static void init(SimulationConfiguration simulationConfiguration) {
        initialEntities = new EntityCollection();
        updatedEntities = new EntityCollection();

        // TODO: save history output path
    }

    public static void close() {
        // TODO: change hardcoded file path to configurable path
        try (FileWriter writer = new FileWriter("history/entities.json")) {
            Map<String, Collection<HistoricEntity>> entries = new HashMap<>();
            initialEntities.getEntityMaps().forEach((historicClass, entities) -> {
                String jsonIdentifier = getJsonIdentifier(historicClass);
                entries.put(jsonIdentifier, entities.values());
            });

            gson.toJson(entries, writer);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error writing entities.json");
        }

        // TODO: copy configuration file
    }

    public static void registerEntity(Entity... entities) {
        for (Entity entity : entities) {
            Class<? extends HistoricEntity> historicClass = getReferenceClass(entity.getClass());
            HistoricEntity historicEntity = instantiateHistoric(entity);
            initialEntities.addToMapFor(historicClass, historicEntity);
            updatedEntities.addToMapFor(historicClass, historicEntity);
        }
    }

    public static void registerEvent(Event event, Entity... entities) {
        JsonObject entry = new JsonObject();

        entry.add("event", new JsonPrimitive(event.getClass().getSimpleName()));

        List<HistoricEntity> historicEntities = new ArrayList<>();

        for (Entity entity : entities) {
            historicClasses.add(getReferenceClass(entity.getClass()));
            historicEntities.add(instantiateHistoric(entity));
        }

        JsonObject changes = serializeChanges(historicEntities);

        if (changes != null) {
            entry.add("changes", changes);
        }

        if (!serializedEntries.containsKey(event.getInstant())) {
            serializedEntries.put(event.getInstant(), new ArrayList<>());
        }

        serializedEntries.get(event.getInstant()).add(entry);

        // TODO: write fixed number of entries to file and clear list

        for (HistoricEntity entity : historicEntities) {
            updatedEntities.addToMapFor(entity.getClass(), entity);
        }
    }

    private static JsonObject serializeChanges(List<HistoricEntity> entities) {
        JsonObject entry = new JsonObject();

        for (Class<? extends HistoricEntity> historicClass : historicClasses) {
            List<JsonObject> subTree = new ArrayList<>();

            String jsonIdentifier = getJsonIdentifier(historicClass);

            for (HistoricEntity entity : entities) {
                HistoricEntity oldEntity = updatedEntities.getMapFor(historicClass).get(entity.getId());

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
                    subTree.add(jsonEntity);
                }
            }

            if (!subTree.isEmpty()) {
                entry.add(jsonIdentifier, gson.toJsonTree(subTree));
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

    private static JsonObject propertyChange(@NotNull Object oldProperty, @NotNull Object newProperty) {
        if (oldProperty.equals(newProperty)) return null;

        JsonObject property = new JsonObject();
        property.add("old", History.gson.toJsonTree(oldProperty));
        property.add("new", History.gson.toJsonTree(newProperty));

        return property;
    }

}
