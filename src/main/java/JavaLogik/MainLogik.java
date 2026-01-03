package JavaLogik;

import DB.Database;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.time.Instant;

// @Luis: Weiß nicht ob man die braucht, und wenn ja wofür
// --- NEU: JDBC-Importe für DB-Persistenz ---
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.HashMap;

// --- NEU: Calendar/TimeZone imports für Berlin-Zeitzone ---
import java.util.Calendar;
import java.util.TimeZone;

// --- NEU: Reflection-Imports für einfache Id-Lesung ---
import java.lang.reflect.Method;
import java.lang.reflect.Field;

public class MainLogik {

    static{
        Demos.demo();
    }

    private static String currentUserName = Demos.getDemoFamilie().getBenutzerNamen().get(0); // vllt noch beim programmstart abfrage

    static {

        // @Luis was auch immer persisitieren ist

        // Demo-Daten initialisieren
        try {
            loadFromDB(); // lade vorhandene Einträge aus der DB ins Programm
        } catch (Throwable ex) {
            System.err.println("Warnung: Persistieren/Laden der Demos in die DB fehlgeschlagen: " + ex.getMessage());
        }
    }

    // Lädt Personen, Kategorien und Termine aus der DB und füllt die Demo-Familie.
    private static void loadFromDB() {
        try (Connection conn = Database.getConnection()) {
            Familie fam = Demos.getDemoFamilie();
            if (fam == null) {
                Demos.demo();
                fam = Demos.getDemoFamilie();
                if (fam == null) return;
            }

            // leere Demo-Listen
            fam.getMitglieder().clear();
            fam.getKategorien().clear();

            // Personen laden
            Map<Integer, Benutzer> personMap = new HashMap<>();
            try (PreparedStatement ps = conn.prepareStatement("SELECT id, name, passwort_hash, rolle FROM person");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String pw = rs.getString("passwort_hash");
                    String rolle = rs.getString("rolle");
                    Benutzer b = new Benutzer(name == null ? "" : name, "", pw == null ? "" : pw, rolle == null ? "" : rolle);
                    fam.benutzerHinzufuegen(b);
                    personMap.put(id, b);
                }
            } catch (SQLException ex) {
                System.err.println("Personen laden fehlgeschlagen: " + ex.getMessage());
            }

            // Kategorien laden
            Map<Integer, Kategorie> categoryMap = new HashMap<>();
            try (PreparedStatement ps = conn.prepareStatement("SELECT id, name, farbe FROM kategorie");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String farbe = rs.getString("farbe");
                    Kategorie k = new Kategorie(name == null ? "" : name, farbe == null ? "" : farbe);
                    fam.kategorieHinzufuegen(k);
                    categoryMap.put(id, k);
                }
            } catch (SQLException ex) {
                System.err.println("Kategorien laden fehlgeschlagen: " + ex.getMessage());
            }

            // Termine laden
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id, titel, beschreibung, startzeit, endzeit, kategorie_id, person_id FROM termin");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    String titel = rs.getString("titel");
                    String beschr = rs.getString("beschreibung");
                    java.sql.Timestamp sTs = rs.getTimestamp("startzeit");
                    java.sql.Timestamp eTs = rs.getTimestamp("endzeit");
                    Instant start = (sTs == null) ? null : sTs.toInstant();
                    Instant end = (eTs == null) ? null : eTs.toInstant();
                    int kId = rs.getInt("kategorie_id");
                    if (rs.wasNull()) kId = -1;
                    int pId = rs.getInt("person_id");
                    if (rs.wasNull()) pId = -1;

                    Kategorie k = (kId > 0) ? categoryMap.get(kId) : null;
                    Benutzer owner = (pId > 0) ? personMap.get(pId) : null;
                    if (owner == null) continue; // ohne Besitzer überspringen

                    Termin t = new Termin(titel, start, end, beschr, k);
                    setIdIfPossible(t, id);
                    owner.getKalender().terminHinzufuegen(t);
                }
            } catch (SQLException ex) {
                System.err.println("Termine laden fehlgeschlagen: " + ex.getMessage());
            }

        } catch (SQLException ex) {
            System.err.println("DB-Verbindung beim Laden fehlgeschlagen: " + ex.getMessage());
        } catch (Throwable ex) {
            System.err.println("Unerwarteter Fehler beim Laden aus DB: " + ex.getMessage());
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
        Benutzer b = MainLogik.getBenutzerPerName(benutzerName);
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
    public static boolean benutzerErstellen(String name, String email, String password, String rolle) {
        if (name == null || name.isBlank()) return false;
        try {
            Familie fam = Demos.getDemoFamilie(); // Da Familie in Demos erstellt wurde
            String korrekteRolle = (rolle == null || rolle.isBlank()) ? "Kind" : rolle.trim();  // Standard Rolle "Kind"
            boolean geklappt =  fam.erstelleBenutzer(name, email, password, korrekteRolle);
            if (geklappt) {
                // DB: einfachen Insert der neuen Person
                try (Connection conn = Database.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                             "INSERT INTO person (name, passwort_hash, rolle) VALUES (?, ?, ?)")) {
                    ps.setString(1, name.trim());
                    ps.setString(2, password == null ? "" : password);
                    ps.setString(3, sanitizeRole(korrekteRolle));
                    ps.executeUpdate();
                } catch (SQLException ex) {
                    System.err.println("Benutzer DB-Insert fehlgeschlagen: " + ex.getMessage());
                }
            }
            return geklappt;
        } catch (Throwable ex) {
            System.err.println("createBenutzer fehlgeschlagen: " + ex.getMessage());
            return false;
        }
    }

    // Liefert Benutzer nach Namen
    public static Benutzer getBenutzerPerName(String name) {
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
    public static Kategorie getKategoriePerName(String name) {
        try {
            Familie fam = Demos.getDemoFamilie();
            return fam.getKategorieByName(name);
        } catch (Throwable ex) {
            System.err.println("getKategoriePerName failed: " + ex.getMessage());
            return null;
        }
    }

    // Fügt einen Termin dem Kalender des aktuellen Benutzers hinzu
    public static void terminHinzufuegen(Termin t) {

        // 1) im Programm lokal speichern
        Benutzer b = getBenutzerPerName(currentUserName);
        b.getKalender().terminHinzufuegen(t);

        // @Luis b+c+d sieht für mich nach unnötig viel aus. vllt ausnahmen löschen - es soll ja nur dem termin der erstellt wird, auch in die db geschrieben werden
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

            // d) Termin einfügen - jetzt mit RETURN_GENERATED_KEYS und ID-Zuweisung
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO termin (titel, beschreibung, startzeit, endzeit, kategorie_id, person_id) VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, t.getTitel());
                ps.setString(2, t.getBeschreibung());
                // Zeit als SQL TIMESTAMP in deutscher Zeitzone speichern
                setTimestampBerlin(ps, 3, t.getStart());
                setTimestampBerlin(ps, 4, t.getEnde());
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

                // generierte ID lesen und dem Termin-Objekt zuweisen
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        long genId = keys.getLong(1);
                        setIdIfPossible(t, genId);
                    }
                }
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

    // Bearbeiten eines Termins
    public static boolean terminBearbeiten(Termin original, String neuerTitel, Instant neuerStart, Instant neuesEnde, String neueBeschreibung, Kategorie neueKategorie) {
        try {
            if (original == null) return false;

            // ID muss vorhanden sein
            Long tid = getIdFromTermin(original);
            if (tid == null || tid <= 0) {
                System.err.println("terminBearbeiten: keine Termin-ID");
                return false;
            }

            Benutzer b = getBenutzerPerName(currentUserName);
            if (b == null) return false;

            // lokal ändern
            boolean localOk = b.getKalender().terminBearbeiten(original, neuerTitel, neuerStart, neuesEnde, neueBeschreibung, neueKategorie);
            if (!localOk) return false;

            // DB-Update per id
            try (Connection conn = Database.getConnection()) {
                conn.setAutoCommit(false);

                Integer kategorieId = null;
                if (neueKategorie != null && neueKategorie.getName() != null && !neueKategorie.getName().isBlank()) {
                    String kname = neueKategorie.getName();
                    try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM kategorie WHERE name = ?")) {
                        ps.setString(1, kname);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) kategorieId = rs.getInt(1);
                        }
                    }
                    if (kategorieId == null) {
                        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO kategorie (name, farbe) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                            ps.setString(1, kname);
                            ps.setString(2, neueKategorie.getFarbe() == null ? "" : neueKategorie.getFarbe());
                            ps.executeUpdate();
                            try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) kategorieId = keys.getInt(1); }
                        }
                    }
                }

                try (PreparedStatement ups = conn.prepareStatement(
                        "UPDATE termin SET titel = ?, beschreibung = ?, startzeit = ?, endzeit = ?, kategorie_id = ? WHERE id = ?")) {
                    ups.setString(1, neuerTitel);
                    ups.setString(2, neueBeschreibung == null ? "" : neueBeschreibung);
                    // Zeiten als SQL TIMESTAMP in deutscher Zeitzone speichern
                    setTimestampBerlin(ups, 3, neuerStart);
                    setTimestampBerlin(ups, 4, neuesEnde);
                    if (kategorieId != null) ups.setInt(5, kategorieId);
                    else ups.setNull(5, java.sql.Types.INTEGER);
                    ups.setLong(6, tid);
                    int updated = ups.executeUpdate();
                    if (updated == 0) {
                        conn.rollback();
                        System.err.println("DB-Update traf keinen Datensatz für id=" + tid);
                        return false;
                    }
                }

                conn.commit();
            } catch (SQLException ex) {
                System.err.println("DB-Update fehlgeschlagen: " + ex.getMessage());
                return false;
            }

            return true;
        } catch (Throwable ex) {
            System.err.println("terminBearbeiten fehlgeschlagen: " + ex.getMessage());
            return false;
        }
    }

    // Löschen eines Termins
    public static boolean terminLoeschen(Termin t) {
        try {
            Benutzer b = getBenutzerPerName(currentUserName);
            b.getKalender().terminLoeschen(t);

            // DB-Löschung nach lokalem Entfernen (einfach): bevorzugt per Termin-ID, sonst Fallback
            try {
                deleteTerminFromDB(currentUserName, t);
            } catch (Throwable ex) {
                System.err.println("DB-Löschung fehlgeschlagen: " + ex.getMessage());
            }

            return true;
        } catch (Throwable ex) {
            System.err.println("Löschen fehlgeschlagen: " + ex.getMessage());
            return false;
        }
    }

    // Prüft, ob ein Termin mit den Terminen des aktuell angemeldeten Benutzers kollidiert
    public static boolean hatKonflikt(Termin t) {
        if (t == null || currentUserName == null) return false;
        try {
            Benutzer b = getBenutzerPerName(currentUserName);
            if (b == null) return false;
            return b.getKalender().konflikt(t);
        } catch (Throwable ex) {
            System.err.println("Konfliktüberprüfung fehlgeschlagen: " + ex.getMessage());
            return false;
        }
    }

    // Erstellen einer Kategorie
    public static boolean kategorieErstellen(String name, String farbe) {
        if (name == null || name.isBlank()) return false;
        try {
            Familie fam = Demos.getDemoFamilie();
            if (fam == null) return false;
            boolean result = fam.erstelleKategorie(name.trim(), farbe == null ? "#4A90E2" : farbe.trim());
            if (!result) return false;

            // DB: einfache Speicherung (versucht, Kategorie in DB anzulegen)
            try (Connection conn = Database.getConnection();
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO kategorie (name, farbe) VALUES (?, ?)")) {
                ps.setString(1, name.trim());
                ps.setString(2, farbe == null ? "" : farbe.trim());
                ps.executeUpdate();
            } catch (SQLException ex) {
                // kurz loggen, lokale Erstellung bleibt
                System.err.println("Kategorie DB-Insert fehlgeschlagen: " + ex.getMessage());
            }

            return true;
        } catch (Throwable ex) {
            System.err.println("Kategorie erstellen fehlgeschlagen: " + ex.getMessage());
            return false;
        }
    }

    // @Luis kein Plan was hier passiert, aber schreibt die Demo-Daten in die DB, falls behalten, verhindern, dass demos bei jedem Start neu reingeschrieben werden
    // Schreibt die Demo-Daten in die Datenbank
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
                        Benutzer b = getBenutzerPerName(name);
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

                        // Termine des Benutzers: prüfen / anlegen
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
                                        // Zeiten als SQL TIMESTAMP in deutscher Zeitzone
                                        setTimestampBerlin(ps, 3, t.getStart());
                                        setTimestampBerlin(ps, 4, t.getEnde());
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

    /* @Luis: Die Methode ist eigentlich unnötig, da die Rolle schon beim Anlegen des Benutzers bereinigt wird.
    Rollen haben wir jetzt eh nicht richtig umgesetzt, also können die auch in die datenbank geschrieben werden wie sie sind. */
    // Hilfsmethode zur Normalisierung von Rollen für DB-Insert (nur ADMIN oder KIND zulassen)
    private static String sanitizeRole(String rolle) {
        if (rolle == null) return "Kind";
        String r = rolle.trim().toUpperCase();
        if ("ADMIN".equals(r)) return "ADMIN";
        // nur zulässige Werte sind ADMIN oder KIND -> alles andere als KIND behandeln
        return "Kind";
    }

    // Liefert den Namen des Nutzers für einen Termin
    public static String getBenutzernameFuerTermin(Termin t) {
        Familie fam = Demos.getDemoFamilie();
        for (Benutzer b : fam.getMitglieder()) {
            if (b.getKalender().getTermine().contains(t)) {
                return b.getName();
            }
        }
        return null;
    }

    // Löscht einen Termin in der DB: zuerst per id (falls vorhanden), sonst per person+start+end+(titel)
    public static void deleteTerminFromDB(String personName, Termin t) {
        if (personName == null || t == null) return;
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);

            // 1) versuche per Termin-ID
            Long tid = getIdFromTermin(t);
            if (tid != null && tid > 0) {
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM termin WHERE id = ?")) {
                    ps.setLong(1, tid);
                    ps.executeUpdate();
                    conn.commit();
                    return;
                } catch (SQLException ex) {
                    System.err.println("Löschen per ID fehlgeschlagen, versuche Fallback: " + ex.getMessage());
                }
            }

            // 2) Fallback: person_id + startzeit + endzeit + titel
            Integer personId = null;
            try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM person WHERE name = ?")) {
                ps.setString(1, personName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) personId = rs.getInt(1);
                }
            }

            if (personId != null) {
                String deleteSql = "DELETE FROM termin WHERE person_id = ? AND startzeit = ? AND endzeit = ? AND titel = ?";
                int deleted = 0;
                try (PreparedStatement ds = conn.prepareStatement(deleteSql)) {
                    ds.setInt(1, personId);
                    // Zeiten als SQL TIMESTAMP in deutscher Zeitzone vergleichen
                    setTimestampBerlin(ds, 2, t.getStart());
                    setTimestampBerlin(ds, 3, t.getEnde());
                    ds.setString(4, t.getTitel() == null ? "" : t.getTitel());
                    deleted = ds.executeUpdate();
                }

                if (deleted == 0) {
                    // noch weniger restriktiv: ohne Titel
                    try (PreparedStatement ds2 = conn.prepareStatement("DELETE FROM termin WHERE person_id = ? AND startzeit = ? AND endzeit = ?")) {
                        ds2.setInt(1, personId);
                        setTimestampBerlin(ds2, 2, t.getStart());
                        setTimestampBerlin(ds2, 3, t.getEnde());
                        ds2.executeUpdate();
                    }
                }
            }

            conn.commit();
        } catch (SQLException ex) {
            System.err.println("DB-Delete fehlgeschlagen (deleteTerminFromDB): " + ex.getMessage());
        } catch (Throwable ex) {
            System.err.println("Unerwarteter Fehler beim DB-Delete (deleteTerminFromDB): " + ex.getMessage());
        }
    }

    // Versuch, eine numerische ID aus dem Termin-Objekt zu lesen (getId(), getDbId(), Feld 'id')
    private static Long getIdFromTermin(Termin t) {
        if (t == null) return null;
        try {
            try {
                Method m = t.getClass().getMethod("getId");
                Object val = m.invoke(t);
                if (val instanceof Number) return ((Number) val).longValue();
            } catch (NoSuchMethodException ignore) {}
            try {
                Method m = t.getClass().getMethod("getDbId");
                Object val = m.invoke(t);
                if (val instanceof Number) return ((Number) val).longValue();
            } catch (NoSuchMethodException ignore) {}
            try {
                Field f = t.getClass().getDeclaredField("id");
                f.setAccessible(true);
                Object val = f.get(t);
                if (val instanceof Number) return ((Number) val).longValue();
            } catch (NoSuchFieldException ignore) {}
        } catch (Throwable ex) {
            System.err.println("getIdFromTermin error: " + ex.getMessage());
        }
        return null;
    }

    // Setzt die ID im Termin-Objekt, wenn möglich (kurz)
    private static void setIdIfPossible(Termin t, long id) {
        if (t == null) return;
        try {
            try {
                Method m = t.getClass().getMethod("setId", long.class);
                m.invoke(t, id);
                return;
            } catch (NoSuchMethodException ignore) {}
            try {
                Method m = t.getClass().getMethod("setId", Long.class);
                m.invoke(t, Long.valueOf(id));
                return;
            } catch (NoSuchMethodException ignore) {}
            try {
                Field f = t.getClass().getDeclaredField("id");
                f.setAccessible(true);
                Class<?> ft = f.getType();
                if (ft == long.class || ft == Long.class) f.set(t, id);
                else if (ft == int.class || ft == Integer.class) f.set(t, (int) id);
            } catch (NoSuchFieldException ignore) {}
        } catch (Throwable ex) {
            System.err.println("setIdIfPossible error: " + ex.getMessage());
        }
    }

    // Helper: speichere Instant als SQL TIMESTAMP in Europe/Berlin
    private static void setTimestampBerlin(PreparedStatement ps, int index, Instant instant) throws SQLException {
        if (instant == null) {
            ps.setNull(index, java.sql.Types.TIMESTAMP);
            return;
        }
        java.sql.Timestamp ts = java.sql.Timestamp.from(instant);
        Calendar berlin = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
        ps.setTimestamp(index, ts, berlin);
    }
}
