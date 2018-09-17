package db61b;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import static org.junit.Assert.*;

public class CommandInterpreterTest {
    private final ByteArrayOutputStream content = new ByteArrayOutputStream();

    @Before
    public void setUp() throws Exception {
        System.setOut(new PrintStream(content));
    }

    @After
    public void tearDown() throws Exception {
        System.setOut(null);
    }

    public String readFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));

    }

    public String runFile(String path) throws IOException {
        String fi = readFile(path);
        Scanner input = new Scanner(fi);
        CommandInterpreter runner = new CommandInterpreter(input, System.out);
        while (input.hasNext()) {
            runner.statement();
        }
        String result = content.toString();
        content.reset();
        return result;

    }


    @Test
    public void createinsertPrintStatement() throws Exception {
        String result = runFile("Ttablepart2.txt");
        assertEquals(readFile("Prinedttabl.txt"), result);

    }


    @Test
    public void loadStatement() throws Exception {

        Files.copy(new File("../testing/enrolled.db").toPath(),
            new File("enrolled.db").toPath());
        String result = runFile("lotest.txt");
        assertEquals(readFile("loteprint.txt"), result);
        Files.delete(new File("enrolled.db").toPath());



    }

    @Test
    public void storeStatement() throws Exception {
        String result = runFile("sttest.txt");
        assertEquals(readFile("stp.txt"), readFile("Classes.db"));
        Files.delete(new File("Classes.db").toPath());
    }


    @Test
    public void selectStatement() throws Exception {
        Files.copy(new File("../testing/enrolled.db").toPath(),
            new File("enrolled.db").toPath());
        Files.copy(new File("../testing/schedule.db").toPath(),
            new File("schedule.db").toPath());
        String result = runFile("stest.txt");
        assertEquals(readFile("selprint.txt"), result);
        Files.delete(new File("enrolled.db").toPath());
        Files.delete(new File("schedule.db").toPath());

    }


}
