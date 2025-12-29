package JavaLogik;

import java.time.Instant;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class Kalender {

    private static int nextId = 1;
    private final int id;
    private List<Termin> termine;

    public Kalender() {
        this.id = nextId++;
        this.termine = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public List<Termin> getTermine() {
        return termine;
    }

    public void terminHinzufuegen(Termin termin) {
        this.termine.add(termin);
    }

    public void terminLoeschen(Termin termin) {
        this.termine.remove(termin);
    }

    public boolean konflikt(Termin neuerTermin) {
        for (Termin bestehenderTermin : termine) {
            if (neuerTermin.getStart().equals(bestehenderTermin.getStart())
                && neuerTermin.getEnde().equals(bestehenderTermin.getEnde())) {
                continue;
            }
            if (neuerTermin.getStart().isBefore(bestehenderTermin.getEnde()) &&
                    neuerTermin.getEnde().isAfter(bestehenderTermin.getStart())) {
                return true; // Konflikt gefunden
            }
        }
        return false; // Kein Konflikt
    }

    public List<Termin> termineSuchen(String name) {   // sucht Termin nach Namen -- nicht genutzt
        List<Termin> gefundeneTermine = new ArrayList<>();
        for (Termin termin : termine) {
            if (termin.getTitel().equals(name)) {
                gefundeneTermine.add(termin);
            }
        }
        if (gefundeneTermine.isEmpty()) {
            return null;    // falls kein Termin gefunden wurde -- vllt anders lösen
        }
        return gefundeneTermine;
    }

    public boolean terminBearbeiten(Termin termin, String neuerTitel, Instant neuerStart, Instant neuesEnde, String neueBeschreibung, Kategorie neueKategorie) {
        if (konflikt(new Termin(neuerTitel, neuerStart, neuesEnde, neueBeschreibung, neueKategorie))) {
            return false;
        }
        termin.setTitel(neuerTitel);
        termin.setDatum(neuerStart);
        termin.setEnde(neuesEnde);
        termin.setBeschreibung(neueBeschreibung);
        termin.setKategorie(neueKategorie);
        return true;
    }

    public List<Termin> getTermineDatum(LocalDate date) {    // gibt alle Termine für ein bestimmtes Datum zurück
        List<Termin> termineDatum = new ArrayList<>();
        ZoneId zone = ZoneId.systemDefault();
        for (Termin t : this.termine) {
            LocalDate d = ZonedDateTime.ofInstant(t.getStart(), zone).toLocalDate();
            if (d.equals(date)) {
                termineDatum.add(t);
            }
        }
        return termineDatum;
    }
}
