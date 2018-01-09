package es.urjc.ia.bikesurbanfleets.usersgenerator.config;

import com.google.gson.JsonElement;
public class UserProperties {

    private String typeName;

    private JsonElement parameters;

    public UserProperties(){}

    public String getTypeName() {
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
