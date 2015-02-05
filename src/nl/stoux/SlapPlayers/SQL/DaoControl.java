package nl.stoux.SlapPlayers.SQL;

import nl.stoux.SlapPlayers.Model.Name;
import nl.stoux.SlapPlayers.SQL.Annotations.Column;
import nl.stoux.SlapPlayers.SQL.Annotations.Table;
import nl.stoux.SlapPlayers.SQL.Model.ColumnField;
import nl.stoux.SlapPlayers.SQL.Model.FullTable;
import nl.stoux.SlapPlayers.SlapPlayers;
import nl.stoux.SlapPlayers.Util.Log;
import nl.stoux.SlapPlayers.Util.ReflectionUtil;
import nl.stoux.SlapPlayers.Util.SUtil;
import org.reflections.Reflections;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by Stoux on 23/01/2015.
 */
public class DaoControl {

    private static HashMap<Class<?>, FullTable> tables = new HashMap<>();

    /**
     * Get the DAO for a class
     * @param daoClass The class of the dao
     * @param <DaoClass>
     * @return The DAO
     */
    public static <DaoClass extends Object> Dao<DaoClass> createDAO(Class<DaoClass> daoClass) {
        return new DaoImpl<>(SlapPlayers.getSQLPool().getConnection(), tables.get(daoClass), daoClass);
    }

    /**
     * Register tables in a package
     * @param classInPackage The class in the package
     */
    //TODO Reflect doesn't behave the way I expected. It also takes the super package. So if you pass "nl.stoux.Package" it will actually search through "nl.stoux"... I think...
    public static void registerTables(Class<?> classInPackage) {
        //Create the Reflections scanner
        Reflections r = ReflectionUtil.reflectPackage(classInPackage);
        Log.info("[DAO] Registering tables for package: '" + classInPackage.getPackage().getName() + "'");

        //Find the tables
        Set<Class<?>> classes = r.getTypesAnnotatedWith(Table.class);
        for (Class<?> aClass : classes) {
            //Get the columns
            Set<Field> columns = ReflectionUtil.getFieldsWithAnnotations(aClass, Column.class);

            //Create the new FoundTable
            FullTable foundTable = new FullTable();
            foundTable.setName(aClass.getAnnotation(Table.class).name());

            //Create map with columns
            List<ColumnField> fields = new ArrayList<>();
            for (Field column : columns) {
                column.setAccessible(true);
                ColumnField cField = new ColumnField();
                Column colAnn = column.getAnnotation(Column.class);
                cField.setType(column.getType());
                cField.setField(column);
                cField.setAutoIncrement(colAnn.autoIncrementID());

                //Check if name is set
                String name = colAnn.name();
                if (name.equals("")) {
                    //Use name of param
                    name = column.getName();
                }
                cField.setName(name);

                //Add to list
                fields.add(cField);
            }
            foundTable.setFields(fields);

            //Store the FoundTable
            tables.put(aClass, foundTable);

            //Log table
            Log.info("[DAO] Registered table '" + foundTable.getName() + "'. Columns: " + SUtil.combineToString(fields, ", ", f -> f.getName()));
        }
    }


}
