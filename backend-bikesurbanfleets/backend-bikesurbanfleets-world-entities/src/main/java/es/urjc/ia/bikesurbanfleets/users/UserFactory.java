package es.urjc.ia.bikesurbanfleets.users;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import es.urjc.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.common.util.MessageGuiFormatter;
import es.urjc.ia.bikesurbanfleets.usersgenerator.UserProperties;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * This class serves to create, in a generic way, user instances.
 * @author IAgroup
 */
public class UserFactory {

    private Set<Class<?>> userClasses;

    private Gson gson = new Gson();

    public UserFactory() {
        // Load User classes by reflection using the annotation UserType
        Reflections reflections = new Reflections();
        this.userClasses = reflections.getTypesAnnotatedWith(UserType.class);
    }


    /**
     * It creates a specific type of user.
     * @param epUserProps It is the user type and parameters which determines the instance type to create.
     * @return an instance of a specific user type.
     */
    public User createUser(UserProperties epUserProps, SimulationServices services) {

        JsonElement parameters;
        parameters = new JsonObject();
        if(epUserProps.getParameters() != null) {
            parameters = epUserProps.getParameters();
        }
        String type = epUserProps.getTypeName();

        User user = instantiateUser(services, parameters, type);
        if (user != null) return user;
        throw new IllegalArgumentException("The type" + epUserProps.getTypeName() + "doesn't exists");
    }

    private User instantiateUser(SimulationServices services, JsonElement parameters, String type) {

        for(Class<?> userClass: userClasses) {
            String userTypeAnnotation = userClass.getAnnotation(UserType.class).value();
            if(userTypeAnnotation.equals(type)) {
                List<Class<?>> innerClasses = Arrays.asList(userClass.getClasses());
                Class<?> userParametersClass = null;

                //Searching parameters class
                for(Class<?> innerClass: innerClasses) {
                    if(innerClass.getSimpleName().equals("UserParameters")) {
                        userParametersClass = innerClass;
                        break;
                    }
                }

                try {
                    if(userParametersClass != null) {
                        Constructor constructor = userClass.getConstructor(userParametersClass, SimulationServices.class);
                        User user = (User) constructor.newInstance(gson.fromJson(parameters, userParametersClass), services);
                        return user;
                    }
                    else {
                        Constructor constructor = userClass.getConstructor(SimulationServices.class);
                        User user = (User) constructor.newInstance(services);
                        return user;
                    }
                }
                catch(Exception e) {
                    MessageGuiFormatter.showErrorsForGui("Error Creating user");
                    MessageGuiFormatter.showErrorsForGui(e);
                }

            }
        }
        return null;
    }
}
