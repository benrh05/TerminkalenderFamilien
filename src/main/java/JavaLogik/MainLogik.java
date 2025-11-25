package JavaLogik;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
}
