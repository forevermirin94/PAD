/*
 * @author Thijs vd Boogaard
 * @doel SQL statement maken voor insert
 *
 * Usage: insert([table voor insert], [naam lijst], [stringarray met waardes]);
 * Usage: getLijst(), returnt stringarray met alle lijsten
 * Usage: getContent([query string]), returnt inhoud van een lijst
 *
 */
package appkiller;

import java.sql.*;
import java.util.Arrays;
import javafx.collections.ObservableList;

public class SQLInsert {

    //deze entries worden door elk object gebruikt, vandaar globale declaration
    Connection c = null;
    Statement state = null;

    //insert: lijst met naam en content in de database zetten
    public void insert(String tables, String naam, ObservableList sqls) {

        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:processes.db");
            c.setAutoCommit(false);

            state = c.createStatement();
            String table = "CREATE TABLE " + tables + " (process VARCHAR(65))";
            state.executeUpdate(table);

            for (int i = 0; i < sqls.size(); i++) {
                String sql = "INSERT INTO " + naam + " (process) VALUES ('" + sqls.get(i) + "');";
                state.executeUpdate(sql);
            }

        } catch (ClassNotFoundException | SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }

        try {
            state.close();
            c.commit();
            c.close();

        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }

        System.out.println("\nSQLInsert finished");
    }

    //getLijst: lijsten ophalen uit database
    public String[] getLijst() {

        try {
            int i = 0;

            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:processes.db");
            c.setAutoCommit(false);

            state = c.createStatement();
            ResultSet namen;
            String tabellen = "SELECT name FROM sqlite_master WHERE type='table'";
            
            namen = state.executeQuery(tabellen);          

            String[] naamlijsten = new String[20];

            while (namen.next()) {
                naamlijsten[i] = namen.getString("name");
                i++;
            }

            namen.close();

            try {
                state.close();
                c.close();

            } catch (Exception e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }

            System.out.println("SQLGetLijst finished: " + i + " lists found");

            //initial array is veel te groot; kopie maken met juiste lengte
            String[] naamlijstenTrim = Arrays.copyOf(naamlijsten, i);

            return naamlijstenTrim;

        } catch (ClassNotFoundException | SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }

        System.exit(0);
        return null;
    }

    //getContent: inhoud van lijsten ophalen uit database
    public String[] getContent(String query) {

        String[] processes = new String[100];

        try {
            int i = 0;

            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:processes.db");
            c.setAutoCommit(false);

            state = c.createStatement();

            ResultSet rs = state.executeQuery(query);
            
            while (rs.next()) {
                String naamprocess = rs.getString("process");
                System.out.println(naamprocess);
                processes[i] = naamprocess;
                i++;
            }

        } catch (ClassNotFoundException | SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }

        try {
            state.close();
            c.close();

        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }

        System.out.println("\nSQLGetContent finished");
        return processes;
    }
}
