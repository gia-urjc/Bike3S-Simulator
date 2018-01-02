package es.urjc.ia.bikesurbanfleets.config.entrypoints;

import com.google.gson.JsonElement;
import es.urjc.ia.bikesurbanfleets.entities.users.UserType;

public class EntryPointUserProperties {

    private UserType typeName;

    private JsonElement parameters;

    public EntryPointUserProperties(){}

    public UserType getTypeName() {
        return typeName;
    }

    public JsonElement getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        String result = "Type Name:" + this.typeName + "\n";
        if(parameters != null) {
            result += "Parameters: \n";
            for(String k: parameters.getAsJsonObject().keySet()) {
                result += k + " : ";
                result += parameters.getAsJsonObject().get(k) + "\n";
            }
        }
        return result;
    }
}
