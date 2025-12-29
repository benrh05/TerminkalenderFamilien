package JavaLogik;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.time.Instant;

// @Luis: Weiß nicht ob man die braucht, und wenn ja für was
// --- NEU: JDBC-Importe für DB-Persistenz ---
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Map;
import java.util.HashMap;

public class MainLogik {

    private static String currentUserName = Demos.getDemoFamilie().getBenutzerNamen().get(0); // vllt noch beim programmstart abfrage

    static {

        // @Luis was auch immer persisitieren ist

        // Demo-Daten initialisieren
        Demos.demo();
        try {
            persistDemosToDB();
        } catch (Throwable ex) {
            System.err.println("Warnung: Persistieren der Demos in die DB fehlgeschlagen: " + ex.getMessage());
        }
    }

    // Aktuellen Benutzer setzen / holen
    public static void setCurrentUserName(String name) {
        currentUserName = name;
    }

    public static String getCurrentUserName() {
        return currentUserName;
    }

    // wenn true -> zeige Termine aller Benutzer der Familie
    private static boolean zeigeAlleTermine = false;

    public static void setAlleTermineAnzeigen(boolean alleTermine) {
        zeigeAlleTermine = alleTermine;
    }

    public static boolean getZeigeAlleTermine() {
        return zeigeAlleTermine;
    }

    public static List<Termin> getTermineFuerDatumFuerNutzer(LocalDate date, String benutzerName) {
        if (Demos.getDemoFamilie() == null) Demos.demo();
        if (benutzerName == null) return new ArrayList<>();
        Benutzer b = MainLogik.getBenutzerByName(benutzerName);
        if (b != null) {
            return b.getKalender().getTermineDatum(date);
        } else {
            return new ArrayList<>();
        }
    }

    // Liefert die Termine für ein bestimmtes Datum (entweder alle Nutzer oder nur eines Nutzers)
    public static List<Termin> getTermineFuerDatum(LocalDate date) {
        try {
            // Wenn Flag gesetzt: aggregiere Termine aller Benutzer in der Demo-Familie
            if (zeigeAlleTermine) {
                List<Termin> all = new ArrayList<>();
                Familie fam = Demos.getDemoFamilie();
                if (fam == null) return all;
                List<Benutzer> mitglieder = fam.getMitglieder();
                if (mitglieder == null) return all;
                for (Benutzer b : mitglieder) {
                    if (b != null && b.getKalender() != null) {
                        List<Termin> t = b.getKalender().getTermineDatum(date);
                        if (t != null && !t.isEmpty()) all.addAll(t);
                    }
                }
                return all;
            } else {
                // Standard: nur Termine des aktuellen Benutzers
                return getTermineFuerDatumFuerNutzer(date, currentUserName);
            }
        } catch (Throwable ex) {
            System.err.println("getTermineDatum failed: " + ex.getMessage());
            return new ArrayList<>();   // bei Fehler leere Liste zurückgeben
        }
    }

    // liefert Benutzernamen aus der Demo-Familie
    public static List<String> getBenutzerNamen() {
        return Demos.getDemoFamilie().getBenutzerNamen();
    }

    // Anlegen eines Benutzers in der Demo-Familie
    public static boolean createBenutzer(String name, String email, String password, String rolle) {
        if (name == null || name.isBlank()) return false;
        try {
            Familie fam = Demos.getDemoFamilie(); // Da Familie in Demos erstellt wurde
            String korrekteRolle = (rolle == null || rolle.isBlank()) ? "Kind" : rolle.trim();  // Standard Rolle "Kind"
            return fam.erstelleBenutzer(name, email, password, korrekteRolle);
        } catch (Throwable ex) {
            System.err.println("createBenutzer fehlgeschlagen: " + ex.getMessage());
            return false;
        }
    }

    // Liefert Benutzer nach Namen
    public static Benutzer getBenutzerByName(String name) {
        if (Demos.getDemoFamilie() == null) Demos.demo(); // Sicherstellen, dass Demo-Familie existiert
        if (name == null) return null;
        for (Benutzer b : Demos.getDemoFamilie().getMitglieder()) {
            if (name.equals(b.getName())) return b;
        }
        return null;
    }

    // Liefert alle Kategoriennamen
    public static List<String> getKategorienNamen() {
        try {
            Familie fam = Demos.getDemoFamilie();
            if (fam == null) return new ArrayList<>();
            return fam.getKategorienNamen();
        } catch (Throwable ex) {
            System.err.println("getKategorienNamen failed: " + ex.getMessage());
            return new ArrayList<>();
        }
    }

    // Liefert Kategorie anhand des Namens
    public static Kategorie getKategorieByName(String name) {
        try {
            Familie fam = Demos.getDemoFamilie();
            if (fam == null) return null;
            return fam.getKategorieByName(name);
        } catch (Throwable ex) {
            System.err.println("getKategorieByName failed: " + ex.getMessage());
            return null;
        }
    }

    // Fügt einen Termin dem Kalender des aktuellen Benutzers hinzu
    public static void addTermin(Termin t) {
        if (t == null) return;

        // 1) in ram hinzufügen
        Benutzer b = getBenutzerByName(currentUserName);
        b.getKalender().terminHinzufuegen(t);

        // @Luis sieht für mich nach unnötig viel aus. vllt ausnahmen löschen
        // 2) Persistenz: versuche, den Termin in die DB zu schreiben (robust / optional)
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            Integer personId = null;

            // a) suche Person nach Namen
            String personName = currentUserName == null ? "" : currentUserName;
            try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM person WHERE name = ?")) {
                ps.setString(1, personName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) personId = rs.getInt(1);
                }
            }

            // b) falls Person nicht existiert: lege sie an (mit einfachem Default-Passwort und Rolle 'KIND')
            if (personId == null) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO person (name, passwort_hash, rolle) VALUES (?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, personName);
                    ps.setString(2, ""); // kein Passwort gesetzt (Default)
                    ps.setString(3, "KIND");
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) personId = keys.getInt(1);
                    }
                }
            }

            // c) Kategorie behandeln (falls vorhanden): suchen / anlegen
            Integer kategorieId = null;
            if (t.getKategorie() != null && t.getKategorie().getName() != null && !t.getKategorie().getName().isBlank()) {
                String kname = t.getKategorie().getName();
                try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM kategorie WHERE name = ?")) {
                    ps.setString(1, kname);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) kategorieId = rs.getInt(1);
                    }
                }

                if (kategorieId == null) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO kategorie (name, farbe) VALUES (?, ?)",
                            Statement.RETURN_GENERATED_KEYS)) {
                        ps.setString(1, kname);
                        ps.setString(2, t.getKategorie().getFarbe() == null ? "" : t.getKategorie().getFarbe());
                        ps.executeUpdate();
                        try (ResultSet keys = ps.getGeneratedKeys()) {
                            if (keys.next()) kategorieId = keys.getInt(1);
                        }
                    }
                }
            }

            // d) Termin einfügen
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO termin (titel, beschreibung, startzeit, endzeit, kategorie_id, person_id) VALUES (?, ?, ?, ?, ?, ?)")) {
                ps.setString(1, t.getTitel());
                ps.setString(2, t.getBeschreibung());
                ps.setTimestamp(3, Timestamp.from(t.getStart()));
                ps.setTimestamp(4, Timestamp.from(t.getEnde()));
                if (kategorieId != null) {
                    ps.setInt(5, kategorieId);
                } else {
                    ps.setNull(5, java.sql.Types.INTEGER);
                }
                if (personId != null) {
                    ps.setInt(6, personId);
                } else {
                    ps.setNull(6, java.sql.Types.INTEGER);
                }
                ps.executeUpdate();
            }

            conn.commit();
        } catch (SQLException ex) {
            // DB-Fehler dürfen die UI nicht crashen — loggen und weiter
            System.err.println("DB-Persistenz fehlgeschlagen (Termin): " + ex.getMessage());
            try {
                // Versuch: keine harte Fehlerbehandlung, Verbindung wird beim Try-With-Resources geschlossen
            } catch (Throwable ignore) {}
        } catch (Throwable ex) {
            System.err.println("Unerwarteter Fehler bei DB-Persistenz (Termin): " + ex.getMessage());
        }
    }

    // Wrapper zum Bearbeiten eines Termins im Kalender des aktuellen Benutzers ---
    public static boolean editTermin(Termin original, String neuerTitel, Instant neuerStart, Instant neuesEnde, String neueBeschreibung, Kategorie neueKategorie) {
        try {
            if (original == null) return false;
            if (currentUserName == null) return false;
            Benutzer b = getBenutzerByName(currentUserName);
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
            Benutzer b = getBenutzerByName(currentUserName);
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
            Benutzer b = getBenutzerByName(currentUserName);
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

    // --- NEU: Wrapper zum Erstellen einer Kategorie auf Familien-Ebene ---
    public static boolean createKategorie(String name, String farbe) {
        if (name == null || name.isBlank()) return false;
        try {
            Familie fam = Demos.getDemoFamilie();
            if (fam == null) return false;
            boolean created = fam.erstelleKategorie(name.trim(), farbe == null ? "#4A90E2" : farbe.trim());
            if (created) {
                try { if (categoryAddedListener != null) categoryAddedListener.accept(name.trim()); } catch (Throwable ignore) {}
            }
            return created;
        } catch (Throwable ex) {
            System.err.println("createKategorie fehlgeschlagen: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Persistiert die Demo-Daten (Benutzer, Kategorien, Termine) in die relationale DB.
     * Verhalten: defensiv, Fehler werden geloggt, sollen aber den Start nicht verhindern.
     */
    private static void persistDemosToDB() {
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // global caches um mehrfaches SELECT/INSERT zu vermeiden
                Map<String, Integer> personCache = new HashMap<>();
                Map<String, Integer> categoryCache = new HashMap<>();

                List<String> names = Demos.getDemoFamilie().getBenutzerNamen();
                if (names == null || names.isEmpty()) {
                    conn.commit();
                    return;
                }

                // --- Neu: Fülle categoryCache einmalig aus den Familien-Kategorien ---
                Familie famForPersist = Demos.getDemoFamilie();
                List<Kategorie> familyCategories = (famForPersist != null) ? famForPersist.getKategorien() : null;
                if (familyCategories != null) {
                    for (Kategorie fk : familyCategories) {
                        if (fk == null || fk.getName() == null || fk.getName().isBlank()) continue;
                        Integer catId = null;
                        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM kategorie WHERE name = ?")) {
                            ps.setString(1, fk.getName());
                            try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) catId = rs.getInt(1);
                            }
                        }
                        if (catId == null) {
                            try (PreparedStatement ps = conn.prepareStatement(
                                    "INSERT INTO kategorie (name, farbe) VALUES (?, ?)",
                                    Statement.RETURN_GENERATED_KEYS)) {
                                ps.setString(1, fk.getName());
                                ps.setString(2, fk.getFarbe() == null ? "" : fk.getFarbe());
                                ps.executeUpdate();
                                try (ResultSet keys = ps.getGeneratedKeys()) {
                                    if (keys.next()) catId = keys.getInt(1);
                                }
                            } catch (SQLException insertEx) {
                                // Insert kann z.B. wegen Race-Condition fehlschlagen -> versuche SELECT erneut
                                try (PreparedStatement ps2 = conn.prepareStatement("SELECT id FROM kategorie WHERE name = ?")) {
                                    ps2.setString(1, fk.getName());
                                    try (ResultSet rs2 = ps2.executeQuery()) {
                                        if (rs2.next()) catId = rs2.getInt(1);
                                    }
                                } catch (SQLException ignore) {}
                            }
                        }
                        if (catId != null) categoryCache.put(fk.getName(), catId);
                    }
                }

                for (String name : names) {
                    try {
                        Benutzer b = getBenutzerByName(name);
                        if (b == null) continue;

                        // --- Person: prüfen / anlegen (robust: SELECT -> INSERT -> SELECT -> ggf. UPDATE) ---
                        Integer personId = null;
                        if (personCache.containsKey(b.getName())) {
                            personId = personCache.get(b.getName());
                        } else {
                            // 1) prüfen ob bereits vorhanden
                            try (PreparedStatement ps = conn.prepareStatement("SELECT id, passwort_hash, rolle FROM person WHERE name = ?")) {
                                ps.setString(1, b.getName());
                                try (ResultSet rs = ps.executeQuery()) {
                                    if (rs.next()) {
                                        personId = rs.getInt("id");
                                    }
                                }
                            }

                            // 2) falls nicht vorhanden: Insert versuchen
                            if (personId == null) {
                                String pw = (b.getPassword() == null) ? "" : b.getPassword();
                                String roleToWrite = sanitizeRole(b.getRolle());
                                try (PreparedStatement ps = conn.prepareStatement(
                                        "INSERT INTO person (name, passwort_hash, rolle) VALUES (?, ?, ?)",
                                        Statement.RETURN_GENERATED_KEYS)) {
                                    ps.setString(1, b.getName());
                                    ps.setString(2, pw);
                                    ps.setString(3, roleToWrite);
                                    ps.executeUpdate();
                                    try (ResultSet keys = ps.getGeneratedKeys()) {
                                        if (keys.next()) personId = keys.getInt(1);
                                    }
                                } catch (SQLException insertEx) {
                                    // Insert kann z.B. wegen Race-Condition oder ENUM-Fehler fehlschlagen.
                                    // Versuche erneut die id zu lesen; falls gefunden, setzen wir personId und fahren fort.
                                    try (PreparedStatement ps2 = conn.prepareStatement("SELECT id FROM person WHERE name = ?")) {
                                        ps2.setString(1, b.getName());
                                        try (ResultSet rs2 = ps2.executeQuery()) {
                                            if (rs2.next()) personId = rs2.getInt(1);
                                        }
                                    } catch (SQLException ignore) {
                                        // nichts weiter tun, wir loggen das ursprüngliche insertEx
                                    }
                                }
                            }

                            // 3) Falls wir jetzt eine personId haben, stelle sicher dass passwort_hash und rolle korrekt gesetzt sind.
                            if (personId != null) {
                                // Lade aktuelle DB-Werte
                                String dbPw = null;
                                String dbRolle = null;
                                try (PreparedStatement ps = conn.prepareStatement("SELECT passwort_hash, rolle FROM person WHERE id = ?")) {
                                    ps.setInt(1, personId);
                                    try (ResultSet rs = ps.executeQuery()) {
                                        if (rs.next()) {
                                            dbPw = rs.getString("passwort_hash");
                                            dbRolle = rs.getString("rolle");
                                        }
                                    }
                                }

                                String desiredPw = (b.getPassword() == null) ? "" : b.getPassword();
                                String desiredRole = sanitizeRole(b.getRolle());

                                boolean needUpdate = false;
                                String updateSql = "UPDATE person SET ";
                                List<Object> params = new ArrayList<>();
                                if ((dbPw == null || dbPw.isEmpty()) && (desiredPw != null && !desiredPw.isEmpty())) {
                                    updateSql += "passwort_hash = ?";
                                    params.add(desiredPw);
                                    needUpdate = true;
                                }
                                // Rolle: falls DB-Rolle null oder unterschiedlich und desiredRole ist valider ENUM -> update
                                if ((dbRolle == null || !dbRolle.equalsIgnoreCase(desiredRole))
                                        && desiredRole != null && !desiredRole.isEmpty()) {
                                    if (needUpdate) updateSql += ", ";
                                    updateSql += "rolle = ?";
                                    params.add(desiredRole);
                                    needUpdate = true;
                                }
                                if (needUpdate) {
                                    updateSql += " WHERE id = ?";
                                    params.add(personId);
                                    try (PreparedStatement ups = conn.prepareStatement(updateSql)) {
                                        for (int i = 0; i < params.size(); i++) {
                                            Object p = params.get(i);
                                            if (p instanceof String) ups.setString(i + 1, (String) p);
                                            else if (p instanceof Integer) ups.setInt(i + 1, (Integer) p);
                                            else ups.setObject(i + 1, p);
                                        }
                                        ups.executeUpdate();
                                    } catch (SQLException uex) {
                                        // Falls UPDATE fehlschlägt -> loggen und weitermachen
                                        System.err.println("Warnung: konnte person (update) nicht anpassen: " + uex.getMessage());
                                    }
                                }
                            }

                            if (personId != null) personCache.put(b.getName(), personId);
                        }

                        // --- Termine des Benutzers: prüfen / anlegen ---
                        Kalender kal = b.getKalender();
                        if (kal != null) {
                            for (Termin t : kal.getTermine()) {
                                if (t == null) continue;
                                try {
                                    // Bestimme kategorie_id (falls vorhanden) - aus family categoryCache (nicht aus kalender)
                                    Integer kId = null;
                                    if (t.getKategorie() != null && t.getKategorie().getName() != null) {
                                        kId = categoryCache.get(t.getKategorie().getName());
                                        if (kId == null) {
                                            // Fallback: suche in DB (sollte aber bereits in cache sein)
                                            try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM kategorie WHERE name = ?")) {
                                                ps.setString(1, t.getKategorie().getName());
                                                try (ResultSet rs = ps.executeQuery()) {
                                                    if (rs.next()) kId = rs.getInt(1);
                                                }
                                            }
                                        }
                                    }

                                    // Einfügen: einfache Strategie ohne Duplikaterkennung (Demo-Daten werden einmalig eingefügt)
                                    try (PreparedStatement ps = conn.prepareStatement(
                                            "INSERT INTO termin (titel, beschreibung, startzeit, endzeit, kategorie_id, person_id) VALUES (?, ?, ?, ?, ?, ?)")) {
                                        ps.setString(1, t.getTitel());
                                        ps.setString(2, t.getBeschreibung());
                                        ps.setTimestamp(3, Timestamp.from(t.getStart()));
                                        ps.setTimestamp(4, Timestamp.from(t.getEnde()));
                                        if (kId != null) {
                                            ps.setInt(5, kId);
                                        } else {
                                            ps.setNull(5, java.sql.Types.INTEGER);
                                        }
                                        if (personCache.get(b.getName()) != null) {
                                            ps.setInt(6, personCache.get(b.getName()));
                                        } else {
                                            ps.setNull(6, java.sql.Types.INTEGER);
                                        }
                                        ps.executeUpdate();
                                    }
                                } catch (SQLException ste) {
                                    // Fehler bei einzelnen Termin -> loggen und weitermachen
                                    System.err.println("Fehler beim Einfügen eines Demo-Termins: " + ste.getMessage());
                                }
                            }
                        }
                    } catch (Throwable inner) {
                        System.err.println("Fehler beim Persistieren des Benutzers '" + name + "': " + inner.getMessage());
                    }
                }

                conn.commit();
            } catch (Throwable t) {
                try { conn.rollback(); } catch (Throwable ignore) {}
                throw t;
            }
        } catch (SQLException ex) {
            System.err.println("Datenbankfehler beim Persistieren der Demos: " + ex.getMessage());
        } catch (Throwable ex) {
            System.err.println("Unerwarteter Fehler beim Persistieren der Demos: " + ex.getMessage());
        }
    }

    // --- NEU: Hilfsmethode zur Normalisierung von Rollen für DB-Insert (nur ADMIN oder KIND zulassen) ---
    private static String sanitizeRole(String rolle) {
        if (rolle == null) return "KIND";
        String r = rolle.trim().toUpperCase();
        if ("ADMIN".equals(r)) return "ADMIN";
        // nur zulässige Werte sind ADMIN oder KIND -> alles andere als KIND behandeln
        return "KIND";
    }

    // Helfer: fügt Demo-Kategorien in die Familie falls noch nicht vorhanden
    private static void addKategorieIfMissingToFamily(String name, String farbe) {
        try {
            Familie fam = Demos.getDemoFamilie();
            if (fam == null) return;
            if (!fam.kategorieDoppelt(name)) {
                fam.kategorieHinzufuegen(new Kategorie(name, farbe));
            }
        } catch (Throwable ex) {
            System.err.println("addKategorieIfMissingToFamily failed: " + ex.getMessage());
        }
    }
}
