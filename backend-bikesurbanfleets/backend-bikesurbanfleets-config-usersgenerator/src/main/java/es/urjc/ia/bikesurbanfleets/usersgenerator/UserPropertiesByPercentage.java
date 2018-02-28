package es.urjc.ia.bikesurbanfleets.usersgenerator;

import com.google.gson.JsonElement;

public class UserPropertiesByPercentage {

    private int percentage;

    private UserProperties userType;

    public UserPropertiesByPercentage() {}

    public int getPercentage() {
        return this.percentage;
    }

    public UserProperties getUserType() {
        return this.userType;
    }

    @Override
    public String toString() {
        return "UserPropertiesByPercentage{" +
                "percentage=" + percentage +
                ", userType=" + userType +
                '}';
    }
}
