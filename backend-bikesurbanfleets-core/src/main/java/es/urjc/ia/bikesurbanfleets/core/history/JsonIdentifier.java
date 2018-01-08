package es.urjc.ia.bikesurbanfleets.core.history;

import java.lang.annotation.*;

/**
 * This interface defines an anotation which determines a identifier for a json file.
 * @author IAgroup
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface JsonIdentifier {
    String value();
}
