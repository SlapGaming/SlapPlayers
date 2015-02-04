package nl.stoux.SlapPlayers.SQL.Model;

import java.sql.SQLException;

/**
 * Created by Stoux on 26/01/2015.
 */
@FunctionalInterface
public interface SqlFunction<T, R> {

    R apply(T t) throws SQLException;


}
