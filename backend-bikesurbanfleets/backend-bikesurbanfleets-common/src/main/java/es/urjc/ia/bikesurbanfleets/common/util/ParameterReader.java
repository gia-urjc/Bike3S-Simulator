/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.common.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.lang.reflect.Field;

/**
 * This class has just a static method that reads json definitions of parameters from a json text
 * to overwrite the values of the fields of the passed objetc with the values defined in json
 * @author IAgroup
 *
 */
public class ParameterReader {
    
    //it takes a jsonobjetc that must have the following form:
    //  ...{
    //    "typeName": "USER_PAPERAT2018_OBHOLGER",
    //    "parameters": {
    //              paremeter1 : value_par1,
    //              parameter2 : value_par2,
    //              ...
    //      }
    //  }
    // the method takes the object param and substitutues the values of its fields, if these values 
    // are the same as defined in the json definition
    // For example, if param.parameter1 exists, it will have the value value_par1 after wards
    // if there is no parameter entry, the object is not altered
    // parameters that do not exist in either para o the json definition are ignired
    public static void getParameters(JsonObject jsondefinition, Object param) throws IllegalArgumentException, IllegalAccessException {
        if (param == null) 
            return;
        //read specific parameters
        JsonObject jsonparameters = jsondefinition.getAsJsonObject("parameters");
        //if no parameters are specified, the original parameters are used
        if (jsonparameters == null) {
            return;
        }

        //if parameters are present substitute their values with the values from the parameters specified in jason
        Field[] fields = param.getClass().getDeclaredFields();
        JsonElement aux;
        Gson gson = new Gson();
        for (Field f : fields) {
            aux = jsonparameters.get(f.getName());
            if (aux != null) {
                f.setAccessible(true);
                f.set(param, gson.fromJson(aux, f.getType()));
            }
        }
        return;
    }

}
