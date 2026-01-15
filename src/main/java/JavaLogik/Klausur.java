package JavaLogik;

import DB.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Klausur {
    public static void main(String[] args) {

        // Fehlerbehandlung in unserem Projekt:

        // Fehlerbehandlung in der Logik (try–catch)
        try {
            Familie fam = Demos.getDemoFamilie();
            return fam.getKategorieByName(name);
        } catch (Throwable ex) {
            System.err.println("getKategoriePerName failed: " + ex.getMessage());
            return null;
        }

        // Fehler signalisieren (Rückgabewert)
        boolean ok = MainLogik.terminLoeschen(t);
        // true = erfolgreich, false = Fehler
        // damit keine Exception an GUI weitergegeben wird

        // GUI zeigt eine verständliche Meldung, keine technischen Details
        if (!ok) {
            new Alert(Alert.AlertType.ERROR,
                    "Termin konnte nicht gelöscht werden.").showAndWait();
        }

        // wäre vllt auch sinnvoll gewesen:
        public class TerminKonfliktException extends Exception {
            public TerminKonfliktException(String message) {
                super(message);
            }
        }
        public static void terminHinzufuegen(Termin neu)
            throws TerminKonfliktException {
            if (hatKonflikt(neu)) {
                throw new TerminKonfliktException(
                        "Der Termin überschneidet sich mit einem bestehenden Termin."
                );
            }
        }
        //--------------------------------------------------------------------------------

        // Drei Schichten Modell / Verteilung:

        /* Im Projekt wurde kein striktes Drei-Schichten-Modell umgesetzt.
        Aufgrund der überschaubaren Anzahl an SQL-Statements und der geringen Komplexität der Datenbankzugriffe
        wurde auf eigene Datenbankklassen verzichtet.
        Stattdessen erfolgt der Datenzugriff zentral in der Mainlogik,
        wodurch zusätzliche Abstraktionsklassen vermieden und die Verständlichkeit des Codes erhöht wurde. */
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO person (name, passwort_hash, rolle) VALUES (?, ?, ?)")) {
            ps.setString(1, name.trim());
            ps.setString(2, password == null ? "" : password);
            ps.setString(3, sanitizeRole(korrekteRolle));
            ps.executeUpdate();
        } catch (SQLException ex) {
            System.err.println("Benutzer DB-Insert fehlgeschlagen: " + ex.getMessage());
        }
        // düfte nicht drinnen sein, sondern sollte methode in datenbankklasse sein

        //------------------------------------------------------------------------------------

        // Datenbankanbindung:
        /* Das Projekt wurde als Maven-Projekt in IntelliJ angelegt.
        Über die pom.xml haben wir den MySQL-JDBC-Treiber als Dependency eingebunden.
        Über eine JDBC-URL verbindet sich das Java-Programm dann mit der lokal laufenden MySQL-Datenbank.
        Die Verbindungsdaten sind in einer eigenen Klasse zentral hinterlegt.
        oder kürzer:
        Wir haben Maven genutzt, um den MySQL-JDBC-Treiber einzubinden,
        und stellen über eine JDBC-URL eine Verbindung zu einer lokalen MySQL-Datenbank her  */

        // pom.xml Ausschnitt:
            <dependency>
                <groupId>com.mysql</groupId>
                <artifactId>mysql-connector-j</artifactId>
                <version>8.2.0</version>
            </dependency>

        // Database.java Ausschnitt:
        private static final String URL =
                "jdbc:mysql://localhost:3306/Terminkalender?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

        //------------------------------------------------------------------------------------

        // ActionListeners in der GUI:
        // verbindet GUI-Elemente mit Logik, also was nach Aktionen passieren soll

        heuteBtn.setOnAction(e -> {    // Listener für "Heute"-Button. e = Event Objekt
            currentMonth = LocalDate.now().withDayOfMonth(1);   // Java Code
            showMonthView();
        });

        //------------------------------------------------------------------------------------

        // Collections im Projekt:

        // Listen (ArrayList) für Sammlungen von Terminen, Benutzern, Kalendern etc.
        private List<Termin> termine;   // im Kalender, sammelt alle Termine eines Nutzers
        // Warum genutzt? Dynamische Größe, einfache Handhabung

        // HashSet damit Benachrichtigungen nur einmal angezeigt werden
        private final Set<String> shownNotifications = new HashSet<>();
        // Warum genutzt? Schnelle Suche, keine Duplikate

        // Hätten auch sowas nutzen können:
        Map<LocalDate, List<Termin>> termineProTag = new HashMap<>();
        // HashMap, um Termine nach Datum zu gruppieren -> schneller Zugriff auf Termine eines bestimmten Tages
    }
}
