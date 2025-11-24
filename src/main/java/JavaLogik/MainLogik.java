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

        // Demo-Kategorien anlegen (zwei Beispiele)
        appKalender.erstelleKategorie("Privat", "#FF6B6B");
        appKalender.erstelleKategorie("Arbeit", "#4A90E2");

        // Demo-Benutzer in der Familie anlegen (kann später aus Persistenz geladen werden)
        demoFamilie.erstelleBenutzer("Max Mustermann", "max@demo", "pass", "user");
        demoFamilie.erstelleBenutzer("Anna Müller", "anna@demo", "pass", "user");
        demoFamilie.erstelleBenutzer("Chris Beispiel", "chris@demo", "pass", "user");
    }

    // Liefert die Termine für ein bestimmtes Datum (Wrapper)    -- vllt später in Kalender verschieben und entfernen -- dozent findets gut so
    public static List<Termin> getTermineForDate(LocalDate date) {
        return appKalender.getTermineForDate(date);
    }

    // Liefert die Benutzernamen aus der Demo-Familie (Wrapper)    -- vllt später in Familie verschieben und entfernen
    public static List<String> getBenutzerNamen() {
        return demoFamilie.getBenutzerNamen();
    }

    // Neuer Wrapper: Kategoriennamen aus dem Kalender
    public static List<String> getKategorienNamen() {
        return appKalender.getKategorienNamen();
    }

    // Neuer Wrapper: liefert Kategorie-Objekt anhand des Namens (oder null)
    public static Kategorie getKategorieByName(String name) {
        return appKalender.getKategorieByName(name);
    }

    public static void addTermin(Termin t) {
        appKalender.terminErstellenUndHinzufuegen(t);
    }
}
