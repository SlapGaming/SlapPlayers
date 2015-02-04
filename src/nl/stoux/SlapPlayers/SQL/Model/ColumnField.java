package nl.stoux.SlapPlayers.SQL.Model;

import lombok.Data;

import java.lang.reflect.Field;

/**
 * Created by Stoux on 23/01/2015.
 */
@Data
public class ColumnField {

    /** The name of the column */
    private String name;

    /** Is auto increment */
    private boolean autoIncrement;

    /** The Field in the class */
    private Field field;

    /** The class */
    private Class<?> type;

}
