package es.urjc.ia.bikesurbanfleets.core.config;

import com.google.gson.JsonObject;

import java.util.List;

public class UsersConfig {

    /**
     * They are all the entry points of the system obtained from the configuration file.
     */
    private List<JsonObject> initialUsers;

    public List<JsonObject> getUsers() {
        return initialUsers;
    }
}
