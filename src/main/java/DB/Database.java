package DB;



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private static final String URL =
            "jdbc:mysql://localhost:3306/Terminkalender?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";      // ggf. anpassen
    private static final String PASSWORD = "";      // wenn wir ein Passwort hinzufügen
    /* Das Projekt wurde als Maven-Projekt in IntelliJ angelegt.
        Über die pom.xml haben wir den MySQL-JDBC-Treiber als Dependency eingebunden.
        Über eine JDBC-URL verbindet sich das Java-Programm dann mit der lokal laufenden MySQL-Datenbank.
        Die Verbindungsdaten sind in einer eigenen Klasse zentral hinterlegt. */
    // pom.xml Ausschnitt:
            <dependency>
                <groupId>com.mysql</groupId>
                <artifactId>mysql-connector-j</artifactId>
                <version>8.2.0</version>
            </dependency>

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println("DB-Verbindung erfolgreich!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

