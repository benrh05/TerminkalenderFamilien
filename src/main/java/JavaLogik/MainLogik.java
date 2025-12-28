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

    // Flag: wenn true -> zeige Termine aller Benutzer der Familie (statt nur des aktuellen Users)
    private static boolean showAllFamilyTermine = false;

    public static void setShowAllFamilyTermine(boolean v) {
        showAllFamilyTermine = v;
    }

    public static boolean isShowAllFamilyTermine() {
        return showAllFamilyTermine;
    }

    // Liefert die Termine für ein bestimmtes Datum (Wrapper) -> jetzt pro aktuellem Benutzer
    public static List<Termin> getTermineForDate(LocalDate date) {
        try {
            if (date == null) return new ArrayList<>();
            // Wenn Flag gesetzt: aggregiere Termine aller Benutzer in der Demo-Familie
            if (showAllFamilyTermine) {
                List<Termin> all = new ArrayList<>();
                Familie fam = Demos.getDemoFamilie();
                if (fam == null) return all;
                List<Benutzer> mitglieder = fam.getMitglieder();
                if (mitglieder == null) return all;
                for (Benutzer b : mitglieder) {
                    try {
                        if (b != null && b.getKalender() != null) {
                            List<Termin> t = b.getKalender().getTermineForDate(date);
                            if (t != null && !t.isEmpty()) all.addAll(t);
                        }
                    } catch (Throwable ignore) {}
                }
                return all;
            } else {
                // Standard: nur Termine des aktuellen Benutzers
                return Demos.getTermineForDateForUser(date, currentUserName);
            }
        } catch (Throwable ex) {
            System.err.println("getTermineForDate failed: " + ex.getMessage());
            return new ArrayList<>();
        }
    }

    // Wrapper: liefert Benutzernamen aus der Demo-Familie (Wrapper)
    public static List<String> getBenutzerNamen() {
        return Demos.getBenutzerNamen();
    }

    // --- NEU: Wrapper zum Anlegen eines Benutzers in der Demo-Familie ---
    public static boolean createBenutzer(String name, String email, String password, String rolle) {
        if (name == null || name.isBlank()) return false;
        try {
            // Delegiere an Demos / Familie
            Familie fam = Demos.getDemoFamilie();
            if (fam == null) return false;
            boolean created = fam.erstelleBenutzer(name.trim(), email == null ? "" : email.trim(), password == null ? "" : password, rolle == null ? "user" : rolle.trim());
            if (!created) return false;

            // Wenn erfolgreich: neues Benutzer-Objekt holen und Demo-Kategorien hinzufügen (falls noch nicht vorhanden)
            try {
                Benutzer neu = Demos.getBenutzerByName(name.trim());
                if (neu != null) {
                    Kalender k = neu.getKalender();
                    // Füge die gleichen Demo-Kategorien wie in Demos.demo hinzu (gedämpfte Töne)
                    // Prüfe nach Name, damit keine Duplikate entstehen
                    addKategorieIfMissing(k, "Privat", "#A85454");
                    addKategorieIfMissing(k, "Arbeit", "#6C8FA3");
                }
            } catch (Throwable innerEx) {
                // Kategorie-Zuweisung darf Fehler nicht zum Fehlschlag der Benutzer-Erstellung machen
                System.err.println("Warnung: Demo-Kategorien konnten nicht zum neuen Benutzer hinzugefügt werden: " + innerEx.getMessage());
            }

            return true;
        } catch (Throwable ex) {
            System.err.println("createBenutzer fehlgeschlagen: " + ex.getMessage());
            return false;
        }
    }

    // Helfer: fügt Kategorie dem Kalender hinzu, falls Name noch nicht existiert
    private static void addKategorieIfMissing(Kalender k, String name, String farbe) {
        if (k == null || name == null) return;
        try {
            Kategorie exist = k.getKategorieByName(name);
            if (exist == null) {
                k.kategorieHinzufuegen(new Kategorie(name, farbe));
            }
        } catch (Throwable ex) {
            // swallow - nicht kritisch
            System.err.println("addKategorieIfMissing failed: " + ex.getMessage());
        }
    }

    // --- NEU: Wrapper: liefert Benutzer-Objekt anhand des Namens ---
    public static Benutzer getBenutzerByName(String name) {
        if (name == null) return null;
        try {
            return Demos.getBenutzerByName(name);
        } catch (Throwable ex) {
            System.err.println("getBenutzerByName failed: " + ex.getMessage());
            return null;
        }
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
            // Kalender.terminBearbeiten führt the eigentliche Bearbeitung durch
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

    // --- NEU: Prüft, ob ein Termin mit den Terminen des aktuell angemeldeten Benutzers kollidiert ---
    public static boolean hasConflictForCurrentUser(Termin t) {
        if (t == null || currentUserName == null) return false;
        try {
            Benutzer b = Demos.getBenutzerByName(currentUserName);
            if (b == null) return false;
            return b.getKalender().konflikt(t);
        } catch (Throwable ex) {
            System.err.println("hasConflictForCurrentUser failed: " + ex.getMessage());
            return false;
        }
    }

    // Listener für UI, wird aufgerufen, wenn eine neue Kategorie erstellt wurde (liefert Namen)
    private static java.util.function.Consumer<String> categoryAddedListener;

    public static void setCategoryAddedListener(java.util.function.Consumer<String> listener) {
        categoryAddedListener = listener;
    }

    // --- NEU: Wrapper zum Erstellen einer Kategorie für den aktuellen Benutzer ---
    public static boolean createKategorie(String name, String farbe) {
        if (name == null || name.isBlank() || currentUserName == null) return false;
        try {
            Benutzer b = Demos.getBenutzerByName(currentUserName);
            if (b == null) return false;
            boolean created = b.getKalender().erstelleKategorie(name.trim(), farbe == null ? "#4A90E2" : farbe.trim());
            if (created) {
                // Informiere registrierten Listener (UI) falls vorhanden
                try {
                    if (categoryAddedListener != null) categoryAddedListener.accept(name.trim());
                } catch (Throwable ignore) {}
            }
            return created;
        } catch (Throwable ex) {
            System.err.println("createKategorie fehlgeschlagen: " + ex.getMessage());
            return false;
        }
    }
}
