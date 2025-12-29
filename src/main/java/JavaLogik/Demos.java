package JavaLogik;

import java.time.*;

// Klasse um alle Demos anzulegen

public class Demos {

    // kein globaler appKalender mehr
    private static Familie demoFamilie;

    public static void demo() {
        if (demoFamilie != null) return; // schon initialisiert

        demoFamilie = new Familie("DemoFamilie");

        // Demo-Benutzer anlegen
        demoFamilie.erstelleBenutzer("Max Mustermann", "max@demo", "pass", "admin");
        demoFamilie.erstelleBenutzer("Anna Müller", "anna@demo", "pass", "admin");
        demoFamilie.erstelleBenutzer("Chris Beispiel", "chris@demo", "pass", "user");

        // Demo-Kategorien anlegen
        Kategorie kPrivat = new Kategorie("Privat", "#A85454");  // rot
        Kategorie kArbeit = new Kategorie("Arbeit", "#6C8FA3");  // grau

        demoFamilie.kategorieHinzufuegen(kPrivat);
        demoFamilie.kategorieHinzufuegen(kArbeit);

        // Hilfswerte für Termine
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(4); // Beispiel in 4 Tagen, um Demo-Termine aktuell zu halten

        // Benutzer-Objekte holen
        Benutzer max = MainLogik.getBenutzerPerName("Max Mustermann");
        Benutzer anna = MainLogik.getBenutzerPerName("Anna Müller");
        Benutzer chris = MainLogik.getBenutzerPerName("Chris Beispiel");

        // Termine für Max (zwei Termine heute) -- eig muss das einfacher sein, indem man die Termine über vorhandene methoden hinzufügt
        if (max != null) {
            Kategorie priv = demoFamilie.getKategorieByName("Privat");  // erst kategorien holen
            Kategorie arbeit = demoFamilie.getKategorieByName("Arbeit");

            Instant t1s = ZonedDateTime.of(today, LocalTime.of(9, 0), zone).toInstant();   // dann Zeiten erstellen (Instant aus LocalDateTime in Zeitzone)
            Instant t1e = ZonedDateTime.of(today, LocalTime.of(10, 30), zone).toInstant();
            max.getKalender().terminHinzufuegen(new Termin("Zahnarzt", t1s, t1e, "Kontrolle und Reinigung", priv));  // dann Termin hinzufügen

            Instant t2s = ZonedDateTime.of(today, LocalTime.of(14, 0), zone).toInstant();
            Instant t2e = ZonedDateTime.of(today, LocalTime.of(15, 0), zone).toInstant();
            max.getKalender().terminHinzufuegen(new Termin("Team-Meeting", t2s, t2e, "Sprint Planung, Raum A2", arbeit));
        }

        // Termine für Anna (ein Termin in 4 Tagen)
        if (anna != null) {
            Kategorie priv = demoFamilie.getKategorieByName("Privat");
            Instant t3s = ZonedDateTime.of(futureDate, LocalTime.of(16, 0), zone).toInstant();
            Instant t3e = ZonedDateTime.of(futureDate, LocalTime.of(17, 30), zone).toInstant();
            anna.getKalender().terminHinzufuegen(new Termin("Yoga-Kurs", t3s, t3e, "Studio Zentrum", priv));
        }

        // Termine für Chris (ein Termin morgen)
        if (chris != null) {
            Kategorie priv = demoFamilie.getKategorieByName("Privat");
            LocalDate morgen = today.plusDays(1);
            Instant t4s = ZonedDateTime.of(morgen, LocalTime.of(11, 0), zone).toInstant();
            Instant t4e = ZonedDateTime.of(morgen, LocalTime.of(12, 0), zone).toInstant();
            chris.getKalender().terminHinzufuegen(new Termin("Arzttermin", t4s, t4e, "Kurztermin", priv));
        }
    }

    public static Familie getDemoFamilie() {
        if (demoFamilie == null) demo();  // sicherstellen, dass Demo erstellt wurde
        return demoFamilie;
    }
}
