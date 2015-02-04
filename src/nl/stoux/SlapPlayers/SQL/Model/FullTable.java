package nl.stoux.SlapPlayers.SQL.Model;

import lombok.Data;

import java.util.List;

/**
 * Created by Stoux on 23/01/2015.
 */
@Data
public class FullTable {

    /** The name of the table in the DB */
    private String name;

    /** The list with columns */
    private List<ColumnField> fields;

}
