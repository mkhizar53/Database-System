
package db61b;



import java.util.HashMap;

/**
 * A collection of Tables, indexed by name.
 *
 * @author Mohammad Khizar
 */
class Database {

    /**
     * Hashmapping thru creating tables assigned @ strings.
     */
    private HashMap<String, Table> tab;

    /**
     * An empty database.
     */
    public Database() {
        tab = new HashMap<>();
    }

    /**
     * Return the Table whose name is NAME stored in this database, or null
     * if there is no such table.
     */
    public Table get(String name) {
        return tab.get(name);
    }

    /**
     * Set or replace the table named NAME in THIS to TABLE.  TABLE and
     * NAME must not be null, and NAME must be a valid name for a table.
     */
    public void put(String name, Table table) {
        if (name == null || table == null) {
            throw new IllegalArgumentException("null argument");
        }
        tab.put(name, table);
    }

}
