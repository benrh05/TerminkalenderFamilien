package DB;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DBTest {
    public static void main(String[] args) {
        try (Connection conn = Database.getConnection()) {
            System.out.println("Verbindung erfolgreich! ");

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM termin");

            rs.next();
            int count = rs.getInt(1);
            System.out.println("Anzahl Termine in der Tabelle: " + count);

        } catch (Exception e) {
            System.out.println("Fehler beim Datenbankzugriff:");
            e.printStackTrace();
        }
    }
}

