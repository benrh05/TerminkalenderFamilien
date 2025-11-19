package JavaLogik;

import java.time.Instant;

public class MainLogik {

    public static void main(String[] args) {
        // was auch immer hier reinkommt, denke mal gui start und so

        // testimplementierung der hauptfunktionen
        Familie testFamilie = Familie.erstelleFamilie("testFamilie"); // Familienname muss aus der GUI kommen
        testFamilie.erstelleBenutzer("Max Mustermann", "max.mustermann@gmail.com", "password123", "Admin"); // Benutzerinformationen müssen aus der GUI kommen
        testFamilie.getMitglieder().get(0).getKalender().erstelleKategorie("Arbeit", "#FF5733"); // Kategorieninformationen müssen aus der GUI kommen
        testFamilie.getMitglieder().get(0).getKalender().terminErstellenUndHinzufuegen("Meeting", Instant.parse("2024-07-01T10:00:00Z"), Instant.parse("2024-07-01T11:00:00Z"), "Projektbesprechung", testFamilie.getMitglieder().get(0).getKalender().getKategorien().get(0)); // Termininformationen müssen aus der GUI kommen
        testFamilie.benutzerBearbeiten(testFamilie.getMitglieder().get(0), "Toni Mustermann", "toni.mustermann@gmail.com", "newpassword456", "User"); // Neue Benutzerinformationen müssen aus der GUI kommen
        testFamilie.getMitglieder().get(0).getKalender().terminBearbeiten(testFamilie.getMitglieder().get(0).getKalender().getTermine().get(0), "Wichtiges Meeting", Instant.parse("2024-07-01T10:30:00Z"), Instant.parse("2024-07-01T11:30:00Z"), "Wichtige Projektbesprechung", testFamilie.getMitglieder().get(0).getKalender().getKategorien().get(0)); // Neue Termininformationen müssen aus der GUI kommenten

    }
}


