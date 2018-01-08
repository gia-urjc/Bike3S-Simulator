package es.urjc.ia.bikesurbanfleets.core.entities.users;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This interface defines an anotation which makes reference to a user type. 
 * @author IAgrup
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AssociatedType {
    UserType value();
}
