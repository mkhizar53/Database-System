package db61b;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class TableTest {
    private final ByteArrayOutputStream content = new ByteArrayOutputStream();
    public String[] columnNames = {
        "SID", "Lastname", "Firstname", "SemEnter", "YearEnter", "Major"
    };

    @Before
    public void setUp() throws Exception {
        System.setOut(new PrintStream(content));
    }

    @After
    public void tearDown() throws Exception {
        System.setOut(null);
    }

    @Test
    public void columns() throws Exception {
        Table testTable = new Table(columnNames);
        assertEquals(6, testTable.columns());
    }

    @Test
    public void getTitle() throws Exception {
        Table testTable = new Table(columnNames);
        assertEquals("Lastname", testTable.getTitle(1));
    }

    @Test
    public void findColumn() throws Exception {
        Table testTable = new Table(columnNames);
        assertEquals(4, testTable.findColumn("YearEnter"));
    }

    @Test
    public void size() throws Exception {
        Table testTable = new Table(columnNames);
        assertEquals(0, testTable.size());
    }

    @Test
    public void get() throws Exception {
        Table testTable = new Table(columnNames);
        String[] row1 = {"101", "Knowles", "Jason", "F", "2003", "EECS"};
        String[] row2 = {"102", "Chan", "Valerie", "S", "2003", "Math"};
        String[] row3 = {"103", "Xavier", "Jonathan", "S", "2004", "LSUnd"};
        String[] row4 = {"101", "Knowles", "Jason", "F", "2003", "EECS"};
        testTable.add(row1);
        testTable.add(row2);
        testTable.add(row3);
        testTable.add(row4);
        assertEquals("Chan", testTable.get(1, 1));
    }

    @Test
    public void add() throws Exception {
        Table testTable = new Table(columnNames);
        String[] row1 = {"101", "Knowles", "Jason", "F", "2003", "EECS"};
        String[] row2 = {"102", "Chan", "Valerie", "S", "2003", "Math"};
        String[] row3 = {"103", "Xavier", "Jonathan", "S", "2004", "LSUnd"};
        String[] row4 = {"101", "Knowles", "Jason", "F", "2003", "EECS"};
        assertEquals(true, testTable.add(row1));
        assertEquals(true, testTable.add(row2));
        assertEquals(true, testTable.add(row3));
        assertEquals(false, testTable.add(row4));
        testTable.print();
    }

    @Test
    public void readTable() throws Exception {
        Files.copy(new File("../testing/enrolled.db").toPath(),
            new File("enrolled.db").toPath());
        Table tab = Table.readTable("enrolled");
        assertEquals("21105", tab.get(1, 1));

        Files.delete(new File("enrolled.db").toPath());


    }

    @Test
    public void writeTable() throws Exception {
        Table testTable = new Table(columnNames);
        String[] row1 = {"101", "Knowles", "Jason", "F", "2003", "EECS"};
        String[] row3 = {"102", "Chan", "Valerie", "S", "2003", "Math"};
        String[] row2 = {"103", "Xavier", "Jonathan", "S", "2004", "LSUnd"};
        String[] row4 = {"101", "Knowles", "Jason", "F", "2003", "EECS"};
        testTable.add(row1);
        testTable.add(row2);
        testTable.add(row3);
        testTable.add(row4);
        testTable.writeTable("testTablename");

        Files.delete(new File("testTablename.db").toPath());


    }

    public String readfile(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)));
    }

    @Test
    public void print() throws Exception {
        Table testTable = new Table(columnNames);
        String[] row1 = {"101", "Knowles", "Jason", "F", "2003", "EECS"};
        String[] row3 = {"102", "Chan", "Valerie", "S", "2003", "Math"};
        String[] row2 = {"103", "Xavier", "Jonathan", "S", "2004", "LSUnd"};
        String[] row4 = {"101", "Knowles", "Jason", "F", "2003", "EECS"};
        testTable.add(row1);
        testTable.add(row2);
        testTable.add(row3);
        testTable.add(row4);
        testTable.print();
        String expectedOutput = readfile("Tp.txt");
        assertEquals(expectedOutput, content.toString());


    }

    @Test
    public void select() throws Exception {
        Table testTable = new Table(columnNames);
        String[] row1 = {"101", "Knowles", "Jason", "F", "2003", "EECS"};
        String[] row3 = {"102", "Chan", "Valerie", "S", "2003", "Math"};
        String[] row2 = {"103", "Xavier", "Jonathan", "S", "2004", "LSUnd"};
        String[] row4 = {"101", "Knowles", "Jason", "F", "2003", "EECS"};
        testTable.add(row1);
        testTable.add(row2);
        testTable.add(row3);
        testTable.add(row4);

        ArrayList<String> nameColumns = new ArrayList<>();
        nameColumns.add("SID");

        ArrayList<Condition> allConditions = new ArrayList<>();
        Table testTabletwo = testTable.select(nameColumns, allConditions);

        assertEquals(testTable.get(2, 0), testTabletwo.get(2, 0));
        assertEquals(testTable.get(0, 0), testTabletwo.get(0, 0));

        nameColumns.add("Lastname");
        Table testTablethree = testTable.select(nameColumns, allConditions);
        assertEquals(testTable.get(2, 1), testTablethree.get(2, 1));

        Column newColumns = new Column("YearEnter", testTable);
        Condition newConditions = new Condition(newColumns, ">=", "2003");
        allConditions.add(newConditions);
        Table testTablefour = testTable.select(nameColumns, allConditions);
        assertEquals(testTable.get(1, 0), testTablefour.get(1, 0));

    }

    @Test
    public void select1() throws Exception {
        Table testTable = new Table(columnNames);
        String[] row1 = {"101", "Knowles", "Jason", "F", "2003", "EECS"};
        String[] row3 = {"102", "Chan", "Valerie", "S", "2003", "Math"};
        String[] row2 = {"103", "Xavier", "Jonathan", "S", "2004", "LSUnd"};
        String[] row4 = {"101", "Knowles", "Jason", "F", "2003", "EECS"};
        testTable.add(row1);
        testTable.add(row2);
        testTable.add(row3);
        testTable.add(row4);

        Table testTabletwo = new Table(new String[]{
            "SID1", "Lastname1", "Firstname1", "SemEnter", "YearEnter1", "Major"
        });
        String[] rOne = {"201", "Know", "Jason", "F", "2005", "EECS"};
        String[] rThree = {"202", "Channing", "Valeria", "S", "2007", "Math"};
        String[] rTwo = {"203", "zavier", "Jonathan", "A", "2010", "LSUnd"};
        String[] rFour = {"201", "Know", "Jason", "B", "2005", "EECS"};
        testTabletwo.add(rOne);
        testTabletwo.add(rThree);
        testTabletwo.add(rTwo);
        testTabletwo.add(rFour);

        ArrayList<String> nameColumns = new ArrayList<>();
        nameColumns.add("SemEnter");
        nameColumns.add("Major");

        ArrayList<Condition> conditions = new ArrayList<>();

        Table test = testTable.select(testTabletwo, nameColumns, conditions);
        assertEquals(testTable.get(0, 3), test.get(0, 0));

        Column newColumns = new Column("SID", testTable);
        Condition newConditions = new Condition(newColumns, "=", "101");
        conditions.add(newConditions);
        Table test2 = testTable.select(testTabletwo, nameColumns, conditions);

        assertEquals(testTable.get(0, 3), test2.get(0, 0));
        assertEquals(testTabletwo.get(0, 5), test2.get(0, 1));

    }

}
