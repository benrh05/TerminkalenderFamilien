package JavaLogik;



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private static final String URL =
            "jdbc:mysql://localhost:3306/Terminkalender?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";      // ggf. anpassen
    private static final String PASSWORD = "";      // wenn wir ein Passwort hinzufügen

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // kleiner Test
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println("DB-Verbindung erfolgreich!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

