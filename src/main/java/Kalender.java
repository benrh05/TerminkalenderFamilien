import java.util.List;
import java.util.ArrayList;

public class Kalender {

    private static int nextId = 1;
    private final int id;
    private List<Termin> termine;
    private List<Kategorie> kategorien;

    public Kalender() {
        this.id = nextId++;
        this.termine = new ArrayList<>();
        this.kategorien = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public List<Termin> getTermine() {
        return termine;
    }

    public List<Kategorie> getKategorien() {
        return kategorien;
    }

    public void addTermin(Termin termin) {
        this.termine.add(termin);
    }

    public void removeTermin(Termin termin) {
        this.termine.remove(termin);
    }

    public void addKategorie(Kategorie kategorie) {
        this.kategorien.add(kategorie);
    }

    public void removeKategorie(Kategorie kategorie) {
        this.kategorien.remove(kategorie);
    }

    public boolean konflikt(Termin neuerTermin) {
        for (Termin bestehenderTermin : termine) {   // alle bestehenden Termine prüfen
            if (neuerTermin.getStart().isBefore(bestehenderTermin.getEnde()) &&
                neuerTermin.getEnde().isAfter(bestehenderTermin.getStart())) {
                return true; // Konflikt gefunden
            }
        }
        return false; // Kein Konflikt
    }

    public void kategorieHinzufuegen(Kategorie kategorie) {
        this.kategorien.add(kategorie);
    }

    public boolean kategorienEnthalten(Kategorie kategorie) {
        return this.kategorien.contains(kategorie);
    }
}
