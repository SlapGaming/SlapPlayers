package nl.stoux.SlapPlayers.SQL.Annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Stoux on 23/01/2015.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {

    /**
     * The name of the column in the DB.
     * If no value is given it will take the name of the attribute field in the class
     * @return the name
     */
    String value() default "";

    /**
     * This is a value that auto increments
     * Shouldn't be inserted
     * @return is auto increment
     */
    boolean autoIncrementID() default false;

}
