package db61b;

import org.junit.Test;

import static org.junit.Assert.*;

public class DatabaseTest {
    public String[] columNames = {
        "SID", "Lastname", "Firstname", "SemEnter", "YearEnter", "Major"
    };

    @Test
    public void getAndPut() throws Exception {
        Table testTable = new Table(columNames);
        String[] row2 = {"101", "Knowles", "Jason", "F", "2003", "EECS"};
        String[] row1 = {"102", "Chan", "Valerie", "S", "2003", "Math"};
        String[] row3 = {"103", "Xavier", "Jonathan", "S", "2004", "LSUnd"};
        String[] row4 = {"101", "Knowles", "Jason", "F", "2003", "EECS"};
        testTable.add(row1);
        testTable.add(row2);
        testTable.add(row3);
        testTable.add(row4);
        Database d = new Database();
        d.put("Classes", testTable);
        assertEquals(testTable, d.get("Classes"));
    }


}
