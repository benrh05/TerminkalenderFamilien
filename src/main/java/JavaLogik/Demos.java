package JavaLogik;

import java.time.*;

// Klasse um alle Demos anzulegen

public class Demos {

    // kein globaler appKalender mehr
    private static Familie demoFamilie;

    public static void demo() {
        if (demoFamilie != null) return; // schon initialisiert

        demoFamilie = new Familie("DemoFamilie");

        // Demo-Benutzer
        demoFamilie.erstelleBenutzer("Max Mustermann", "max@demo", "pass", "admin");
        demoFamilie.erstelleBenutzer("Anna Müller", "anna@demo", "pass", "admin");
        demoFamilie.erstelleBenutzer("Chris Beispiel", "chris@demo", "pass", "user");

        // Demo-Kategorien
        Kategorie kPrivat = new Kategorie("Privat", "#A85454");      // dezentes Rot
        Kategorie kArbeit = new Kategorie("Arbeit", "#6C8FA3");      // dezentes Grau-Blau
        Kategorie kUni = new Kategorie("Uni", "#6EBE67");           // dezentes Grün

        demoFamilie.kategorieHinzufuegen(kPrivat);
        demoFamilie.kategorieHinzufuegen(kArbeit);
        demoFamilie.kategorieHinzufuegen(kUni);

        // Hilfswerte für Termine
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(4); // Beispiel in 4 Tagen, um Demo-Termine aktuell zu halten

        // Benutzer-Objekte holen
        Benutzer max = MainLogik.getBenutzerPerName("Max Mustermann");
        Benutzer anna = MainLogik.getBenutzerPerName("Anna Müller");
        Benutzer chris = MainLogik.getBenutzerPerName("Chris Beispiel");

        // Termine für Max (4 Termine)
        if (max != null) {
            Kategorie priv = demoFamilie.getKategorieByName("Privat");
            Kategorie arbeit = demoFamilie.getKategorieByName("Arbeit");
            Kategorie uni = demoFamilie.getKategorieByName("Uni");

            // 1) Heute
            Instant t1s = ZonedDateTime.of(today, LocalTime.of(9, 0), zone).toInstant();
            Instant t1e = ZonedDateTime.of(today, LocalTime.of(10, 30), zone).toInstant();
            max.getKalender().terminHinzufuegen(new Termin("Zahnarzt", t1s, t1e, "Kontrolle und Reinigung", priv));

            // 2) in 2 Tagen
            Instant t2s = ZonedDateTime.of(today.plusDays(2), LocalTime.of(14, 0), zone).toInstant();
            Instant t2e = ZonedDateTime.of(today.plusDays(2), LocalTime.of(15, 0), zone).toInstant();
            max.getKalender().terminHinzufuegen(new Termin("Team-Meeting", t2s, t2e, "Sprint Planung, Raum A2", arbeit));

            // 3) in 4 Tagen
            Instant t3s = ZonedDateTime.of(today.plusDays(4), LocalTime.of(11, 0), zone).toInstant();
            Instant t3e = ZonedDateTime.of(today.plusDays(4), LocalTime.of(12, 30), zone).toInstant();
            max.getKalender().terminHinzufuegen(new Termin("Vorlesung: Software-Architektur", t3s, t3e, "Vorlesungssaal 3", uni));

            // 4) in 7 Tagen
            Instant t4s = ZonedDateTime.of(today.plusDays(7), LocalTime.of(18, 0), zone).toInstant();
            Instant t4e = ZonedDateTime.of(today.plusDays(7), LocalTime.of(20, 0), zone).toInstant();
            max.getKalender().terminHinzufuegen(new Termin("Geburtstag (Party)", t4s, t4e, "Feier bei Freunden", null));
        }

        // Termine für Anna (5 Termine)
        if (anna != null) {
            Kategorie priv = demoFamilie.getKategorieByName("Privat");
            Kategorie arbeit = demoFamilie.getKategorieByName("Arbeit");
            Kategorie uni = demoFamilie.getKategorieByName("Uni");

            LocalDate base = today.plusDays(3); // alle Termine nach heute

            // 1) in 3 Tagen
            Instant a1s = ZonedDateTime.of(base, LocalTime.of(9, 0), zone).toInstant();
            Instant a1e = ZonedDateTime.of(base, LocalTime.of(10, 0), zone).toInstant();
            anna.getKalender().terminHinzufuegen(new Termin("Vorlesung: Mathe", a1s, a1e, "Hörsaal B", uni));

            // 2) in 3 Tagen
            Instant a2s = ZonedDateTime.of(base, LocalTime.of(11, 0), zone).toInstant();
            Instant a2e = ZonedDateTime.of(base, LocalTime.of(12, 0), zone).toInstant();
            anna.getKalender().terminHinzufuegen(new Termin("Arzttermin", a2s, a2e, "Routineuntersuchung", priv));

            // 3) in 2 Tagen
            Instant a3s = ZonedDateTime.of(base.plusDays(2), LocalTime.of(14, 0), zone).toInstant();
            Instant a3e = ZonedDateTime.of(base.plusDays(2), LocalTime.of(15, 30), zone).toInstant();
            anna.getKalender().terminHinzufuegen(new Termin("Projekt-Meeting", a3s, a3e, "Online-Call", arbeit));

            // 4) in 4 Tagen
            Instant a4s = ZonedDateTime.of(base.plusDays(4), LocalTime.of(8, 30), zone).toInstant();
            Instant a4e = ZonedDateTime.of(base.plusDays(4), LocalTime.of(9, 30), zone).toInstant();
            anna.getKalender().terminHinzufuegen(new Termin("Morgensport", a4s, a4e, "Joggen im Park", null));

            // 5) in 5 Tagen
            Instant a5s = ZonedDateTime.of(base.plusDays(5), LocalTime.of(16, 0), zone).toInstant();
            Instant a5e = ZonedDateTime.of(base.plusDays(5), LocalTime.of(17, 0), zone).toInstant();
            anna.getKalender().terminHinzufuegen(new Termin("Study Group", a5s, a5e, "Gruppenraum 2", uni));
        }

        // Termine für Chris (3 Termine)
        if (chris != null) {
            Kategorie priv = demoFamilie.getKategorieByName("Privat");
            Kategorie arbeit = demoFamilie.getKategorieByName("Arbeit");
            Kategorie uni = demoFamilie.getKategorieByName("Uni");

            // 1) morgen
            LocalDate morgen = today.plusDays(1);
            Instant c1s = ZonedDateTime.of(morgen, LocalTime.of(11, 0), zone).toInstant();
            Instant c1e = ZonedDateTime.of(morgen, LocalTime.of(12, 0), zone).toInstant();
            chris.getKalender().terminHinzufuegen(new Termin("Zahnarzt (Chris)", c1s, c1e, "Kurztermin", priv));

            // 2) in 5 Tagen
            Instant c2s = ZonedDateTime.of(today.plusDays(5), LocalTime.of(15, 0), zone).toInstant();
            Instant c2e = ZonedDateTime.of(today.plusDays(5), LocalTime.of(16, 0), zone).toInstant();
            chris.getKalender().terminHinzufuegen(new Termin("Bewerbungsgespräch", c2s, c2e, "Vorstellungsgespräch", arbeit));

            // 3) in 9 Tagen
            Instant c3s = ZonedDateTime.of(today.plusDays(9), LocalTime.of(10, 0), zone).toInstant();
            Instant c3e = ZonedDateTime.of(today.plusDays(9), LocalTime.of(11, 30), zone).toInstant();
            chris.getKalender().terminHinzufuegen(new Termin("Tutorium: Algorithmen", c3s, c3e, "Sitzung im Raum 5", uni));
        }
    }

    public static Familie getDemoFamilie() {
        if (demoFamilie == null) demo();  // sicherstellen, dass Demo erstellt wurde
        return demoFamilie;
    }
}
