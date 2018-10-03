package es.urjc.ia.bikesurbanfleets.usersgenerator.entrypoint;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.reflections.Reflections;

import java.util.Set;

/**
 * This class serves to create, in a generic way, entry point instances.
 * @author IAgroup
 *
 */
public class EntryPointFactory {

    private Set<Class<?>> entryPointClasses;

    private Gson gson = new Gson();

    public EntryPointFactory() {
        //Load entry points by reflection using the annotation EntryPointType
        Reflections reflections = new Reflections();
        this.entryPointClasses = reflections.getTypesAnnotatedWith(EntryPointType.class);
    }

    /**
     * It creates an entry point of a specific type.
     * @param json it contains the entry point information.
     * @param epType It is the type of entry point which determines the instance type to create
     * @return an instance of specific entry point type.
     */
    public EntryPoint createEntryPoint(JsonObject json, String epType) {
        for(Class<?> entryPointClass: entryPointClasses) {
            String epTypeAnnotation = entryPointClass.getAnnotation(EntryPointType.class).value();
            if(epTypeAnnotation.equals(epType)) {
                return (EntryPoint) gson.fromJson(json, entryPointClass);
            }
        }
        throw new IllegalArgumentException("The type of entry point " + epType + "doesn't exists");
    }

}

