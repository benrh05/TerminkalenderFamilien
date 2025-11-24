package JavaLogik;

import java.time.LocalDate;
import java.util.List;

public class MainLogik {

    static {
        // Demo-Daten zentral in Demos kapseln und einmal initialisieren
        Demos.demo();
    }

    // Liefert die Termine für ein bestimmtes Datum (Wrapper)
    public static List<Termin> getTermineForDate(LocalDate date) {
        return Demos.getTermineForDate(date);
    }

    // Liefert die Benutzernamen aus der Demo-Familie (Wrapper)
    public static List<String> getBenutzerNamen() {
        return Demos.getBenutzerNamen();
    }

    // Wrapper: Kategoriennamen aus dem Kalender
    public static List<String> getKategorienNamen() {
        return Demos.getKategorienNamen();
    }

    // Wrapper: liefert Kategorie-Objekt anhand des Namens (oder null)
    public static Kategorie getKategorieByName(String name) {
        return Demos.getKategorieByName(name);
    }

    public static void addTermin(Termin t) {
        Demos.addTermin(t);
    }
}
