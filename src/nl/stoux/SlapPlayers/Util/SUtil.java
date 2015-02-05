package nl.stoux.SlapPlayers.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by Stoux on 23/01/2015.
 */
public class SUtil {

    /**
     * Check if an array contains a certain object
     * @param array The array
     * @param object The object
     * @param <T> The class of the objects
     * @return contains the object
     */
    public static <T extends Object> boolean contains(T object, T... array) {
        return Arrays.stream(array).anyMatch(t -> t.equals(object));
    }

    /**
     * Build a string from an array of objects
     * @param objects The objects
     * @param separator The separator between objects
     * @param function The function that creates the String
     * @param <T1> The class of the object
     * @return The combined string
     */
    public static <T1 extends Object> String combineToString(T1[] objects, String separator, Function<T1, String> function) {
        String buildString = "";
        for (T1 object : objects) {
            if (!buildString.isEmpty() && separator != null) {
                buildString += separator;
            }

            buildString += function.apply(object);
        }
        return buildString;
    }

    /**
     * Build a string from a collection of objects
     * @param objects The objects
     * @param separator The separator between objects
     * @param function The function that creates the String per object
     * @param <T1> The class of the object
     * @return The combined string
     */
    public static <T1 extends Object> String combineToString(Collection<T1> objects, String separator, Function<T1, String> function) {
        String buildString = "";
        for (T1 object : objects) {
            if (!buildString.isEmpty() && separator != null) {
                buildString += separator;
            }
            buildString += function.apply(object);
        }
        return buildString;
    }

    /**
     * Execute a fuction on all entries. Execute a special function for the first.
     * @param objects The objects
     * @param first The function for the first
     * @param others The function for the others
     * @param <T1> The class of the object
     */
    public static <T1 extends Object> void executeSpecialFirst(Collection<T1> objects, Consumer<T1> first, Consumer<T1> others) {
        boolean isFirst = true;
        for (T1 object : objects) {
            if (isFirst) {
                first.accept(object);
                isFirst = false;
            } else {
                others.accept(object);
            }
        }
    }

    /**
     * Create an ArrayList out of a list of items
     * @param items The items
     * @param <T1> The class of the objects
     * @return the ArrayList
     */
    public static <T1 extends Object> ArrayList<T1> toArrayList(T1... items) {
        return toArrayList(Stream.of(items));
    }

    /**
     * Create an ArrayList out of a Stream with items
     * @param stream The stream
     * @param <T1> The class of the objects
     * @return The ArrayList
     */
    public static <T1 extends Object> ArrayList<T1> toArrayList(Stream<T1> stream) {
        return stream.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }


}
