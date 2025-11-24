package JavaLogik;

import java.time.*;
import java.util.List;

/**
 * Kapselt alle Demo-Daten (Kalender, Familie, Kategorien, Termine, Benutzer).
 * MainLogik ruft Demos.demo() einmal auf und verwendet die Getter/Wrapper.
 */
public class Demos {

    private static Kalender appKalender;
    private static Familie demoFamilie;

    public static void demo() {
        if (appKalender != null && demoFamilie != null) return; // schon initialisiert

        appKalender = new Kalender();
        demoFamilie = new Familie("DemoFamilie");

        // Demo-Kategorien anlegen (zwei Beispiele)
        Kategorie kPrivat = new Kategorie("Privat", "#FF6B6B");
        Kategorie kArbeit = new Kategorie("Arbeit", "#4A90E2");
        appKalender.kategorieHinzufuegen(kPrivat);
        appKalender.kategorieHinzufuegen(kArbeit);

        // Zeitzone / Datum
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now();

        // Konkretes Future-Date (keine "in 4 days"-Beschriftung)
        LocalDate futureDate = LocalDate.of(2025, 11, 28);

        // Termin 1 - heute 09:00 - 10:30 (Privat)
        Instant t1s = ZonedDateTime.of(today, LocalTime.of(9, 0), zone).toInstant();
        Instant t1e = ZonedDateTime.of(today, LocalTime.of(10, 30), zone).toInstant();
        appKalender.terminErstellenUndHinzufuegen(new Termin("Zahnarzt", t1s, t1e, "Kontrolle und Reinigung", kPrivat));

        // Termin 2 - heute 14:00 - 15:00 (Arbeit)
        Instant t2s = ZonedDateTime.of(today, LocalTime.of(14, 0), zone).toInstant();
        Instant t2e = ZonedDateTime.of(today, LocalTime.of(15, 0), zone).toInstant();
        appKalender.terminErstellenUndHinzufuegen(new Termin("Team-Meeting", t2s, t2e, "Sprint Planung, Raum A2", kArbeit));

        // Termin 3 - konkretes Datum 28.11.2025 16:00 - 17:30 (Arbeit)
        Instant t3s = ZonedDateTime.of(futureDate, LocalTime.of(16, 0), zone).toInstant();
        Instant t3e = ZonedDateTime.of(futureDate, LocalTime.of(17, 30), zone).toInstant();
        appKalender.terminErstellenUndHinzufuegen(new Termin("Yoga-Kurs", t3s, t3e, "Studio Zentrum", kPrivat));

        // Demo-Benutzer in der Familie anlegen
        demoFamilie.erstelleBenutzer("Max Mustermann", "max@demo", "pass", "user");
        demoFamilie.erstelleBenutzer("Anna Müller", "anna@demo", "pass", "user");
        demoFamilie.erstelleBenutzer("Chris Beispiel", "chris@demo", "pass", "user");
    }

    // Getter / Wrapper für MainLogik

    public static Kalender getAppKalender() {
        if (appKalender == null) demo();
        return appKalender;
    }

    public static Familie getDemoFamilie() {
        if (demoFamilie == null) demo();
        return demoFamilie;
    }

    public static List<Termin> getTermineForDate(LocalDate date) {
        if (appKalender == null) demo();
        return appKalender.getTermineForDate(date);
    }

    public static List<String> getBenutzerNamen() {
        if (demoFamilie == null) demo();
        return demoFamilie.getBenutzerNamen();
    }

    public static List<String> getKategorienNamen() {
        if (appKalender == null) demo();
        return appKalender.getKategorienNamen();
    }

    public static Kategorie getKategorieByName(String name) {
        if (appKalender == null) demo();
        return appKalender.getKategorieByName(name);
    }

    public static void addTermin(Termin t) {
        if (appKalender == null) demo();
        appKalender.terminErstellenUndHinzufuegen(t);
    }
}
