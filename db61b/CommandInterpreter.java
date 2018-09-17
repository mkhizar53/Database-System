package db61b;


import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

import static db61b.Utils.*;

/**
 * An object that reads and interprets a sequence of commands from an
 * input source.
 *
 * @author Mohammad Khizar
 */
class CommandInterpreter {

    /* STRATEGY.
     *
     *   This interpreter parses commands using a technique called
     * "recursive descent." The idea is simple: we convert the BNF grammar,
     * as given in the specification document, into a program.
     *
     * First, we break up the input into "tokens": strings that correspond
     * to the "base case" symbols used in the BNF grammar.  These are
     * keywords, such as "select" or "create"; punctuation and relation
     * symbols such as ";", ",", ">="; and other names (of columns or tables).
     * All whitespace and comments get discarded in this process, so that the
     * rest of the program can deal just with things mentioned in the BNF.
     * The class Tokenizer performs this breaking-up task, known as
     * "tokenizing" or "lexical analysis."
     *
     * The rest of the parser consists of a set of functions that call each
     * other (possibly recursively, although that isn't needed for this
     * particular grammar) to operate on the sequence of tokens, one function
     * for each BNF rule. Consider a rule such as
     *
     *    <create statement> ::= create table <table name> <table definition> ;
     *
     * We can treat this as a definition for a function named (say)
     * createStatement.  The purpose of this function is to consume the
     * tokens for one create statement from the remaining token sequence,
     * to perform the required actions, and to return the resulting value,
     * if any (a create statement has no value, just side-effects, but a
     * select clause is supposed to produce a table, according to the spec.)
     *
     * The body of createStatement is dictated by the right-hand side of the
     * rule.  For each token (like create), we check that the next item in
     * the token stream is "create" (and report an error otherwise), and then
     * advance to the next token.  For a metavariable, like <table definition>,
     * we consume the tokens for <table definition>, and do whatever is
     * appropriate with the resulting value.  We do so by calling the
     * tableDefinition function, which is constructed (as is createStatement)
     * to do exactly this.
     *
     * Thus, the body of createStatement would look like this (_input is
     * the sequence of tokens):
     *
     *    _input.next("create");
     *    _input.next("table");
     *    String name = name();
     *    Table table = tableDefinition();
     *    _input.next(";");
     *
     * plus other code that operates on name and table to perform the function
     * of the create statement.  The .next method of Tokenizer is set up to
     * throw an exception (DBException) if the next token does not match its
     * argument.  Thus, any syntax error will cause an exception, which your
     * program can catch to do error reporting.
     *
     * This leaves the issue of what to do with rules that have alternatives
     * (the "|" symbol in the BNF grammar).  Fortunately, our grammar has
     * been written with this problem in mind.  When there are multiple
     * alternatives, you can always tell which to pick based on the next
     * unconsumed token.  For example, <table definition> has two alternative
     * right-hand sides, one of which starts with "(", and one with "as".
     * So all you have to do is test:
     *
     *     if (_input.nextIs("(")) {
     *         _input.next("(");
     *         // code to process "<column name>,  )"
     *     } else {
     *         // code to process "as <select clause>"
     *     }
     *
     * As a convenience, you can also write this as
     *
     *     if (_input.nextIf("(")) {
     *         // code to process "<column name>,  )"
     *     } else {
     *         // code to process "as <select clause>"
     *     }
     *
     * combining the calls to .nextIs and .next.
     *
     * You can handle the list of <column name>s in the preceding in a number
     * of ways, but personally, I suggest a simple loop:
     *
     *     ... = columnName();
     *     while (_input.nextIs(",")) {
     *         _input.next(",");
     *         ... = columnName();
     *     }
     *
     * or if you prefer even greater concision:
     *
     *     ... = columnName();
     *     while (_input.nextIf(",")) {
     *         ... = columnName();
     *     }
     *
     * (You'll have to figure out what do with the names you accumulate, of
     * course).
     */


    /**
     * A new CommandInterpreter executing commands read from INP, writing
     * prompts on PROMPTER, if it is non-null.
     */
    CommandInterpreter(Scanner inp, PrintStream prompter) {
        _input = new Tokenizer(inp, prompter);
        _database = new Database();
    }

    /**
     * Parse and execute one statement from the token stream.  Return true
     * iff the command is something other than quit or exit.
     */
    boolean statement() {
        switch (_input.peek()) {
        case "create":
            createStatement();
            break;
        case "load":
            loadStatement();
            break;
        case "exit":
        case "quit":
            exitStatement();
            return false;
        case "*EOF*":
            return false;
        case "insert":
            insertStatement();
            break;
        case "print":
            printStatement();
            break;
        case "select":
            selectStatement();
            break;
        case "store":
            storeStatement();
            break;
        default:
            throw error("unrecognizable command");
        }
        return true;
    }

    /**
     * Parse and execute a create statement from the token stream.
     */
    void createStatement() {
        _input.next("create");
        _input.next("table");
        String n = name();
        Table tae = tableDefinition();
        _database.put(n, tae);
        _input.next(";");
    }

    /**
     * Parse and execute an exit or quit statement. Actually does nothing
     * except check syntax, since statement() handles the actual exiting.
     */
    void exitStatement() {
        if (!_input.nextIf("quit")) {
            _input.next("exit");
        }
        _input.next(";");
    }

    /**
     * Parse and execute an insert statement from the token stream.
     */
    void insertStatement() {
        _input.next("insert");
        _input.next("into");
        Table table = tableName();
        _input.next("values");
        int chu = table.columns();

        String[] vas = new String[chu];

        while (true) {
            _input.next("(");
            for (int i = 0; i < vas.length; i++) {

                vas[i] = literal();

                if (!_input.nextIf(",")) {

                    break;
                }

            }
            _input.next(")");

            table.add(vas);
            if (!_input.nextIf(",")) {

                break;
            }
        }
        _input.next(";");
    }

    /**
     * Parse and execute a load statement from the token stream.
     */
    void loadStatement() {
        _input.next("load");
        String tName = name();
        Table actualTable = Table.readTable(tName);
        _database.put(tName, actualTable);
        System.out.printf("Loaded %s.db%n", tName);
        _input.next(";");
    }

    /**
     * Parse and execute a store statement from the token stream.
     */
    void storeStatement() {
        _input.next("store");
        String ne = _input.peek();
        Table ta = tableName();
        ta.writeTable(ne);
        System.out.printf("Stored %s.db%n", ne);
        _input.next(";");
    }

    /**
     * Parse and execute a print statement from the token stream.
     */
    void printStatement() {
        _input.next("print");
        String naam = "Contents of " + _input.peek() + ":";
        Table tabl = tableName();
        System.out.println(naam);
        tabl.print();
        _input.next(";");

    }

    /**
     * Parse and execute a select statement from the token stream.
     */
    void selectStatement() {
        Table tab = selectClause();
        System.out.println("Search results:");
        tab.print();
        _input.next(";");
    }

    /**
     * Parse and execute a table definition, returning the specified
     * table.
     */
    Table tableDefinition() {
        Table tab;
        if (_input.nextIf("(")) {
            ArrayList<String> var = new ArrayList<>();
            while (!_input.nextIs(")")) {

                var.add(_input.next());

                if (!_input.nextIf(",")) {
                    break;
                }
            }
            _input.next(")");

            tab = new Table(var);

        } else {
            _input.next("as");

            tab = selectClause();
        }
        return tab;
    }

    /**
     * Parse and execute a select clause from the token stream, returning the
     * resulting table.
     */
    Table selectClause() {
        _input.next("select");
        String nOfc = columnName();

        ArrayList<String> columnNames = new ArrayList<>();

        columnNames.add(nOfc);

        while (_input.nextIf(",")) {

            nOfc = columnName();

            columnNames.add(nOfc);
        }
        _input.next("from");

        Table tOn = tableName();

        Table tTw = null;

        if (_input.nextIf(",")) {

            tTw = tableName();
        }

        ArrayList<Condition> conditions = new ArrayList<>();
        if (tTw == null) {
            if (_input.nextIs("where")) {
                conditions = conditionClause(tOn);
            }
            Table res = tOn.select(columnNames, conditions);
            return res;
        } else {
            if (_input.nextIs("where")) {
                conditions = conditionClause(tOn, tTw);
            }
            Table re = tOn.select(tTw, columnNames, conditions);
            return re;
        }
    }

    /**
     * Parse and return a valid name (identifier) from the token stream.
     */
    String name() {
        return _input.next(Tokenizer.IDENTIFIER);
    }

    /**
     * Parse and return a valid column name from the token stream. Column
     * names are simply names; we use a different method name to clarify
     * the intent of the code.
     */
    String columnName() {
        return name();
    }

    /**
     * Parse a valid table name from the token stream, and return the Table
     * that it designates, which must be loaded.
     */
    Table tableName() {
        String n = name();
        Table tab = _database.get(n);
        if (tab == null) {
            throw error("unknown table: %s", n);
        }
        return tab;
    }

    /**
     * Parse a literal and return the string it represents (i.e., without
     * single quotes).
     */
    String literal() {

        String litty = _input.next(Tokenizer.LITERAL);

        return litty.substring(1, litty.length() - 1).trim();
    }


    /**
     * Parse and return a list of Conditions that apply to TABLES from the
     * token stream.  This denotes the conjunction (`and') of zero
     * or more Conditions.
     */
    ArrayList<Condition> conditionClause(Table... tables) {
        ArrayList<Condition> alCondcu = new ArrayList<Condition>();

        _input.next("where");

        Condition conOne = condition(tables);

        alCondcu.add(conOne);

        while (_input.nextIf("and")) {

            Condition neCond = condition(tables);

            alCondcu.add(neCond);
        }
        return alCondcu;
    }

    /**
     * Parse and return a Condition that applies to TABLES from the
     * token stream.
     */
    Condition condition(Table... tables) {
        Column colTwo = null;

        String temp = null;

        String coOne = columnName();

        Column coOn = new Column(coOne, tables);

        String rel = _input.next(Tokenizer.RELATION);

        if (_input.nextIs(Tokenizer.LITERAL)) {

            temp = literal();
            return new Condition(coOn, rel, temp);

        } else {

            temp = columnName();

            colTwo = new Column(temp, tables);

            return new Condition(coOn, rel, colTwo);
        }
    }

    /**
     * Advance the input past the next semicolon.
     */
    void skipCommand() {
        while (true) {
            try {
                while (!_input.nextIf(";") && !_input.nextIf("*EOF*")) {
                    _input.next();
                }
                return;
            } catch (DBException excp) {
                /* No action */
            }
        }
    }

    /**
     * The command input source.
     */
    private Tokenizer _input;
    /**
     * Database containing all tables.
     */
    private Database _database;
}
