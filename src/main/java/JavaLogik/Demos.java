package JavaLogik;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Kapselt alle Demo-Daten (Familie, Benutzer, Kategorien, Termine).
 * MainLogik ruft Demos.demo() einmal auf und verwendet die Getter/Wrapper.
 */
public class Demos {

    // kein globaler appKalender mehr
    private static Familie demoFamilie;

    public static void demo() {
        if (demoFamilie != null) return; // schon initialisiert

        demoFamilie = new Familie("DemoFamilie");

        // Demo-Benutzer in der Familie anlegen (zuerst Benutzer erzeugen)
        demoFamilie.erstelleBenutzer("Max Mustermann", "max@demo", "pass", "user");
        demoFamilie.erstelleBenutzer("Anna Müller", "anna@demo", "pass", "user");
        demoFamilie.erstelleBenutzer("Chris Beispiel", "chris@demo", "pass", "user");

        // Kategorien anlegen (zwei Beispiele) und JEDEM Benutzer-Kalender hinzufügen
        Kategorie kPrivat = new Kategorie("Privat", "#FF6B6B");
        Kategorie kArbeit = new Kategorie("Arbeit", "#4A90E2");

        // Hilfswerte für Termine
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(4); // Beispiel: in 4 Tagen

        // Benutzer-Objekte holen
        Benutzer max = getBenutzerByName("Max Mustermann");
        Benutzer anna = getBenutzerByName("Anna Müller");
        Benutzer chris = getBenutzerByName("Chris Beispiel");

        // Kategorien in die Kalender der Benutzer eintragen (jedem Benutzer die gleichen Demo-Kategorien)
        for (Benutzer b : demoFamilie.getMitglieder()) {
            if (b != null) {
                b.getKalender().kategorieHinzufuegen(new Kategorie(kPrivat.getName(), kPrivat.getFarbe()));
                b.getKalender().kategorieHinzufuegen(new Kategorie(kArbeit.getName(), kArbeit.getFarbe()));
            }
        }

        // Termine für Max (zwei Termine heute)
        if (max != null) {
            Instant t1s = ZonedDateTime.of(today, LocalTime.of(9, 0), zone).toInstant();
            Instant t1e = ZonedDateTime.of(today, LocalTime.of(10, 30), zone).toInstant();
            max.getKalender().terminErstellenUndHinzufuegen(new Termin("Zahnarzt", t1s, t1e, "Kontrolle und Reinigung", max.getKalender().getKategorieByName("Privat")));

            Instant t2s = ZonedDateTime.of(today, LocalTime.of(14, 0), zone).toInstant();
            Instant t2e = ZonedDateTime.of(today, LocalTime.of(15, 0), zone).toInstant();
            max.getKalender().terminErstellenUndHinzufuegen(new Termin("Team-Meeting", t2s, t2e, "Sprint Planung, Raum A2", max.getKalender().getKategorieByName("Arbeit")));
        }

        // Termine für Anna (ein Termin in 4 Tagen)
        if (anna != null) {
            Instant t3s = ZonedDateTime.of(futureDate, LocalTime.of(16, 0), zone).toInstant();
            Instant t3e = ZonedDateTime.of(futureDate, LocalTime.of(17, 30), zone).toInstant();
            anna.getKalender().terminErstellenUndHinzufuegen(new Termin("Yoga-Kurs", t3s, t3e, "Studio Zentrum", anna.getKalender().getKategorieByName("Privat")));
        }

        // Termine für Chris (ein Termin morgen)
        if (chris != null) {
            LocalDate morgen = today.plusDays(1);
            Instant t4s = ZonedDateTime.of(morgen, LocalTime.of(11, 0), zone).toInstant();
            Instant t4e = ZonedDateTime.of(morgen, LocalTime.of(12, 0), zone).toInstant();
            chris.getKalender().terminErstellenUndHinzufuegen(new Termin("Arzttermin", t4s, t4e, "Kurztermin", chris.getKalender().getKategorieByName("Privat")));
        }
    }

    // Getter / Wrapper für MainLogik

    public static Familie getDemoFamilie() {
        if (demoFamilie == null) demo();
        return demoFamilie;
    }

    // Liefert Termine eines bestimmten Benutzers (nach Name) für ein Datum
    public static List<Termin> getTermineForDateForUser(LocalDate date, String benutzerName) {
        if (demoFamilie == null) demo();
        if (benutzerName == null) return new ArrayList<>();
        Benutzer b = getBenutzerByName(benutzerName);
        if (b != null) {
            return b.getKalender().getTermineForDate(date);
        } else {
            return new ArrayList<>();
        }
    }

    // Liefert die Benutzernamen in der Demo-Familie
    public static List<String> getBenutzerNamen() {
        if (demoFamilie == null) demo();
        return demoFamilie.getBenutzerNamen();
    }

    // Liefert die Kategoriennamen für einen bestimmten Benutzer (oder leere Liste)
    public static List<String> getKategorienNamenForUser(String benutzerName) {
        List<String> empty = new ArrayList<>();
        if (demoFamilie == null) demo();
        if (benutzerName == null) return empty;
        Benutzer b = getBenutzerByName(benutzerName);
        if (b != null) {
            return b.getKalender().getKategorienNamen();
        }
        return empty;
    }

    // Fallback: fügt Termin in den ersten Benutzer-Kalender, falls kein aktueller Benutzer gesetzt ist
    public static void addTermin(Termin t) {
        if (demoFamilie == null) demo();
        List<Benutzer> mitglieder = demoFamilie.getMitglieder();
        if (!mitglieder.isEmpty()) {
            mitglieder.get(0).getKalender().terminErstellenUndHinzufuegen(t);
        }
    }

    // Liefert Benutzer-Objekt nach Namen (oder null)
    public static Benutzer getBenutzerByName(String name) {
        if (demoFamilie == null) demo();
        if (name == null) return null;
        for (Benutzer b : demoFamilie.getMitglieder()) {
            if (name.equals(b.getName())) return b;
        }
        return null;
    }
}
