package nl.stoux.SlapPlayers.Util;

import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Stoux on 26/01/2015.
 */
public class ReflectionUtil {

    /**
     * Create a Reflections instance for a certain package
     * @param classInPackage the package name for that class will be used
     * @return the reflections instance
     */
    public static Reflections reflectPackage(Class<?> classInPackage) {
        return new Reflections(
                ClasspathHelper.forPackage(classInPackage.getPackage().toString().replace("package ", "")),
                classInPackage.getClassLoader()
        );
    }

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
     * Get all fields (including private) with one or more annotations
     * @param clazz The class to search the methods in
     * @param annotations The annotations we're looking for
     * @return The set with methods
     */
    public static Set<Method> getMethodsWithAnnotations(Class<?> clazz, Class<? extends Annotation>... annotations) {
        Set<Method> methods = new HashSet<>();
        for (Method method : clazz.getDeclaredMethods()) {
            for (Annotation annotation : method.getDeclaredAnnotations()) {
                if (containsAnnotation(annotation.annotationType(), annotations)) {
                    methods.add(method);
                    break;
                }
            }
        }
        return methods;
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
