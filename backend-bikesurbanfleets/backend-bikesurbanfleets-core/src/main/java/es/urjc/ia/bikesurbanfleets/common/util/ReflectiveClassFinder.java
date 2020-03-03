/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.common.util;

import com.google.gson.JsonObject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;
import org.reflections.Reflections;

/**
 *
 * @author holger
 */
public class ReflectiveClassFinder {
    static private Reflections reflections = new Reflections("es.urjc.ia.bikesurbanfleets");
    
    public static Class findClass(JsonObject jsondescription, Class annotationclasstype) throws Exception {
        if (jsondescription == null) {
            return null;
        }
        String type = jsondescription.get("typeName").getAsString();
        if (type.equals("none")) {
            return null;
        }

        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(annotationclasstype);
        for (Class<?> this_class : classes) {
            Annotation a = this_class.getAnnotation(annotationclasstype);
            Method m = a.getClass().getMethod("value");
            String typeAnnotation = (String) m.invoke(a);
            if (typeAnnotation.equals(type)) {
                return this_class;
            }
        }
        throw new RuntimeException(annotationclasstype.getName() + " " + type + " not found or incorrect");
    }
    
}
