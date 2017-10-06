package com.urjc.iagroup.bikesurbanfloats.history;

import com.google.gson.*;
import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.entities.Bike;
import com.urjc.iagroup.bikesurbanfloats.entities.Entity;
import com.urjc.iagroup.bikesurbanfloats.entities.Person;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.events.EventUserAppears;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class History {
	
	
    static Gson gson = new Gson();

    private static HistoryEntry lastEntry;
    private static HistoryEntry nextEntry;
    
    private static ChangeSerializerPerson changesPerson = new ChangeSerializerPerson();
    private static ChangeSerializerBike changesBike = new ChangeSerializerBike();
    private static ChangeSerializerStation changesStation = new ChangeSerializerStation();
    

    private static ArrayList<JsonObject> serializedEntries = new ArrayList<>();
    
    static JsonObject idChange(Entity oldEntity, Entity newEntity) {
        JsonObject id = new JsonObject();

        if (oldEntity == null && newEntity == null) return null;

        if (oldEntity == null && newEntity != null) {
        	id.add("old", gson.toJsonTree("null"));
            id.add("new", new JsonPrimitive(newEntity.getId()));
            return id;
        }

        if (oldEntity != null && newEntity == null) {
            id.add("old", new JsonPrimitive(oldEntity.getId()));
            id.add("new", gson.toJsonTree("null"));
            return id;
        }

        if (oldEntity.equals(newEntity)) {
            id.add("old", new JsonPrimitive(oldEntity.getId()));
            id.add("new", new JsonPrimitive(newEntity.getId()));
            return id;
        }

        return null;
    }

    public static void init(List<EventUserAppears> userAppearsList) {
        nextEntry = new HistoryEntry(0);
        nextEntry.getStations().putAll(SystemInfo.stations.stream().collect(Collectors.toMap(Station::getId, Station::new)));
        JsonObject entry = new JsonObject();
        
        List<JsonObject> users = new ArrayList<>();
        
        for (EventUserAppears event : userAppearsList) {
            JsonObject serializedUser = new JsonObject();
            Person person = event.getUser();
            nextEntry.getUsers().put(person.getId(), person.copy());
            
            serializedUser.add("appearsOn", new JsonPrimitive(event.getInstant()));
            serializedUser.add("user", gson.toJsonTree(person, Person.class));

            users.add(serializedUser);
        }
        nextEntry.getBikes().putAll(SystemInfo.bikes.stream().collect(Collectors.toMap(Bike::getId, Bike::new)));
           
        entry.add("users", gson.toJsonTree(users));
        entry.add("stations", gson.toJsonTree(SystemInfo.stations));
        entry.add("bikes", gson.toJsonTree(SystemInfo.bikes));
        
        serializedEntries.add(entry);
    }

    public static void register(int timeInstant) {
        if (timeInstant > nextEntry.getTimeInstant()) {
            serializedEntries.add(serializeChanges());
            lastEntry = nextEntry;
        }
        nextEntry = new HistoryEntry(timeInstant);
        nextEntry.getStations().putAll(SystemInfo.stations.stream().collect(Collectors.toMap(Station::getId, Station::new)));
        SystemInfo.persons.stream().map(person -> nextEntry.getUsers().put(person.getId(), person.copy()));
        nextEntry.getBikes().putAll(SystemInfo.bikes.stream().collect(Collectors.toMap(Bike::getId, Bike::new)));
    }

    private static JsonObject serializeChanges() {
        JsonObject entry = new JsonObject();

        List<JsonObject> users = new ArrayList<>();
        List<JsonObject> stations = new ArrayList<>();

        for (Person user : nextEntry.getUsers().values()) {
        	Person oldPerson = lastEntry.getUsers().get(user.getId());
        	Person newPerson = nextEntry.getUsers().get(user.getId());
            JsonObject changes = changesPerson.getChanges(oldPerson, newPerson);
            if (changes != null) users.add(changes);
        }

        if (!users.isEmpty()) {
            entry.add("users", gson.toJsonTree(users));
        }

        for (Station station: nextEntry.getStations().values()) {
        	Station oldStation = lastEntry.getStations().get(station.getId());
        	Station newStation = nextEntry.getStations().get(station.getId());
        	JsonObject changes = changesStation.getChanges(oldStation, newStation);
        	if (changes != null) stations.add(changes);
        }
        
        if(!stations.isEmpty()) {
        	entry.add("stations", gson.toJsonTree(stations));
        }

        return entry;
    }

	

}
