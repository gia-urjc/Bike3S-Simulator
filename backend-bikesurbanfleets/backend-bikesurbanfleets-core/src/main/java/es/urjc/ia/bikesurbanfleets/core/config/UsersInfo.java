package es.urjc.ia.bikesurbanfleets.core.config;

import es.urjc.ia.bikesurbanfleets.usersgenerator.SingleUser;

import java.util.List;

public class UsersInfo {

    /**
     * They are all the entry points of the system obtained from the configuration file.
     */
    private List<SingleUser> initialUsers;

    public List<SingleUser> getUsers() {
        return initialUsers;
    }
}
