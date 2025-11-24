package JavaLogik;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

public class MainLogik {

    // Einfacher zentraler Kalender für die Demo / App-Logik
    private static final Kalender appKalender = new Kalender();

    // Demo-Familie (für Benutzer-Liste)
    private static final Familie demoFamilie = new Familie("DemoFamilie");

    static {
        // Beispieltermine: zwei Termine für "heute" und ein Termin an einem konkreten zukünftigen Datum
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now();

        LocalDate futureDate = LocalDate.of(2025, 11, 28); // <-- hier konkretes Datum eintragen

        // Termin 1 - heute 09:00 - 10:30
        Instant t1s = LocalDateTime.of(today, LocalTime.of(9, 0)).atZone(zone).toInstant();
        Instant t1e = LocalDateTime.of(today, LocalTime.of(10, 30)).atZone(zone).toInstant();
        appKalender.terminErstellenUndHinzufuegen(new Termin("Zahnarzt", t1s, t1e, "Kontrolle und Reinigung", null));

        // Termin 2 - heute 14:00 - 15:00
        Instant t2s = LocalDateTime.of(today, LocalTime.of(14, 0)).atZone(zone).toInstant();
        Instant t2e = LocalDateTime.of(today, LocalTime.of(15, 0)).atZone(zone).toInstant();
        appKalender.terminErstellenUndHinzufuegen(new Termin("Team-Meeting", t2s, t2e, "Sprint Planung, Raum A2", null));

        // Termin 3 - konkretes Datum (z.B. 28.11.2025) 16:00 - 17:30
        Instant t3s = LocalDateTime.of(futureDate, LocalTime.of(16, 0)).atZone(zone).toInstant();
        Instant t3e = LocalDateTime.of(futureDate, LocalTime.of(17, 30)).atZone(zone).toInstant();
        appKalender.terminErstellenUndHinzufuegen(new Termin("Yoga-Kurs", t3s, t3e, "Studio Zentrum", null));

        // Demo-Benutzer in der Familie anlegen (kann später aus Persistenz geladen werden)
        demoFamilie.erstelleBenutzer("Max Mustermann", "max@demo", "pass", "user");
        demoFamilie.erstelleBenutzer("Anna Müller", "anna@demo", "pass", "user");
        demoFamilie.erstelleBenutzer("Chris Beispiel", "chris@demo", "pass", "user");
    }

    /**
     * Liefert alle Termine, deren Startdatum im angegebenen LocalDate liegt.
     * Gibt eine leere Liste zurück, falls keine Termine vorhanden sind.
     */
    public static List<Termin> getTermineForDate(LocalDate date) {
        List<Termin> result = new ArrayList<>();
        ZoneId zone = ZoneId.systemDefault();
        for (Termin t : appKalender.getTermine()) {
            LocalDate d = ZonedDateTime.ofInstant(t.getStart(), zone).toLocalDate();
            if (d.equals(date)) {
                result.add(t);
            }
        }
        return result;
    }

    // Liefert die Benutzernamen aus der Demo-Familie (Wrapper)
    public static List<String> getBenutzerNamen() {
        return demoFamilie.getBenutzerNamen();
    }

    public static void addTermin(Termin t) {
        appKalender.terminErstellenUndHinzufuegen(t);
    }
}
