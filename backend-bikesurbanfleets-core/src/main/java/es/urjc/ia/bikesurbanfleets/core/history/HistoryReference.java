package es.urjc.ia.bikesurbanfleets.core.history;

import java.lang.annotation.*;

/**
 * This interface defines an anotation which is used for an entity refers to its 
 * corresponding history class.
 * @author IAgroup
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface HistoryReference {
    Class<? extends HistoricEntity> value();
}
