package es.urjc.ia.bikesurbanfleets.worldentities.users;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.common.util.MessageGuiFormatter;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
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
     * @param userdef It is the user jasonobject corresponding to the definition of the user 
     * in the configuration file.
     * @return an instance of a specific user type.
     */
    public User createUser(JsonObject userdef, SimulationServices services, int seed) {
 
        User user=null;
        //find the usertype
        String type=userdef.getAsJsonObject("userType").get("typeName").getAsString();
        
        //find the constructoir of the type
        for(Class<?> userClass: userClasses) {
            String userTypeAnnotation = userClass.getAnnotation(UserType.class).value();
            if(userTypeAnnotation.equals(type)) {
                 try {
                    Constructor constructor = userClass.getConstructor(JsonObject.class, SimulationServices.class, long.class  );
                    user = (User) constructor.newInstance(userdef, services, seed);
                    break;
                }
                catch(Exception e) {
                    MessageGuiFormatter.showErrorsForGui("Error Creating user");
                    MessageGuiFormatter.showErrorsForGui(e);
                }

            }
        }
        
        if (user != null) return user;
        throw new IllegalArgumentException("The type" + type + "doesn't exists");
    }

 }
