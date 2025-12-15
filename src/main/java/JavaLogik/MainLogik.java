package JavaLogik;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.time.Instant; // ...neu

public class MainLogik {

    // aktueller Benutzername (wird bei Start auf ersten Demo-Benutzer gesetzt)
    private static String currentUserName = null;

    static {
        // Demo-Daten zentral in Demos kapseln und einmal initialisieren
        Demos.demo();
        // initialen Benutzer setzen (falls vorhanden)
        List<String> names = Demos.getBenutzerNamen();
        if (names != null && !names.isEmpty()) {
            currentUserName = names.get(0);
        } else {
            currentUserName = "Gast";
        }
    }

    // Aktuellen Benutzer setzen / holen
    public static void setCurrentUserName(String name) {
        currentUserName = name;
    }

    public static String getCurrentUserName() {
        return currentUserName;
    }

    // Liefert die Termine für ein bestimmtes Datum (Wrapper) -> jetzt pro aktuellem Benutzer
    public static List<Termin> getTermineForDate(LocalDate date) {
        return Demos.getTermineForDateForUser(date, currentUserName);
    }

    // Liefert die Benutzernamen aus der Demo-Familie (Wrapper)
    public static List<String> getBenutzerNamen() {
        return Demos.getBenutzerNamen();
    }

    // Wrapper: Kategoriennamen aus dem Kalender des aktuellen Benutzers
    public static List<String> getKategorienNamen() {
        if (currentUserName == null) return new ArrayList<>();
        return Demos.getKategorienNamenForUser(currentUserName);
    }

    // Wrapper: liefert Kategorie-Objekt anhand des Namens aus dem Kalender des aktuellen Benutzers (oder null)
    public static Kategorie getKategorieByName(String name) {
        if (currentUserName == null) return null;
        Benutzer b = Demos.getBenutzerByName(currentUserName);
        if (b != null) {
            return b.getKalender().getKategorieByName(name);
        }
        return null;
    }

    // Fügt einen Termin dem Kalender des aktuellen Benutzers hinzu (nicht global)
    public static void addTermin(Termin t) {
        if (currentUserName == null) {
            // fallback: in den ersten Benutzer-Kalender
            Demos.addTermin(t);
            return;
        }
        Benutzer b = Demos.getBenutzerByName(currentUserName);
        if (b != null) {
            b.getKalender().terminErstellenUndHinzufuegen(t);
        } else {
            Demos.addTermin(t); // fallback
        }
    }

    // --- NEU: Wrapper zum Bearbeiten eines Termins im Kalender des aktuellen Benutzers ---
    public static boolean editTermin(Termin original, String neuerTitel, Instant neuerStart, Instant neuesEnde, String neueBeschreibung, Kategorie neueKategorie) {
        try {
            if (original == null) return false;
            if (currentUserName == null) return false;
            Benutzer b = Demos.getBenutzerByName(currentUserName);
            if (b == null) return false;
            // Kalender.terminBearbeiten führt die eigentliche Bearbeitung durch
            return b.getKalender().terminBearbeiten(original, neuerTitel, neuerStart, neuesEnde, neueBeschreibung, neueKategorie);
        } catch (Throwable ex) {
            // bei Fehlern false zurückgeben
            return false;
        }
    }

    // --- NEU: Wrapper zum Löschen eines Termins im Kalender des aktuellen Benutzers ---
    public static boolean deleteTermin(Termin t) {
        if (t == null || currentUserName == null) return false;
        try {
            Benutzer b = Demos.getBenutzerByName(currentUserName);
            if (b == null) return false;
            b.getKalender().terminLoeschen(t);
            // Falls Persistenz/DB genutzt wird, würde hier ein DB-Delete-Aufruf ergänzt werden.
            return true;
        } catch (Throwable ex) {
            System.err.println("Löschen fehlgeschlagen: " + ex.getMessage());
            return false;
        }
    }
}
