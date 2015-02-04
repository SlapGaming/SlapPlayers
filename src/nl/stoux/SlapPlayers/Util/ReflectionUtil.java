package nl.stoux.SlapPlayers.Util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Stoux on 26/01/2015.
 */
public class ReflectionUtil {

    /**
     * Get all fields (including private) with one or more annotations
     * @param clazz The class
     * @param annotations The annotations
     * @return Set with fields that have one of those annotations
     */
    public static Set<Field> getFieldsWithAnnotations(Class<?> clazz, Class<? extends Annotation>... annotations) {
        Set<Field> fields = new HashSet<>();
        for (Field field : clazz.getDeclaredFields()) {
            for (Annotation annotation : field.getDeclaredAnnotations()) {
                if (containsAnnotation(annotation.annotationType(), annotations)) {
                    fields.add(field);
                    break;
                }
            }
        }
        return fields;
    }

    /**
     * Check if the given annotation matches one of the given classes
     * @param annotation The needle
     * @param annotationClasses The haystack
     * @return contains the annotation
     */
    private static boolean containsAnnotation(Class<? extends Annotation> annotation, Class<? extends Annotation>... annotationClasses) {
        for (Class<? extends Annotation> aClass : annotationClasses) {
            if (annotation.equals(aClass)) {
                return true;
            }
        }
        return false;
    }


}
