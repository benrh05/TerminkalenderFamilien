package JavaLogik;

import java.time.Instant;
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

    public void terminErstellenUndHinzufuegen(Termin termin) {
        this.termine.add(termin);
    }

    public void terminLoeschen(Termin termin) {
        this.termine.remove(termin);
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

    public List<Termin> termineSuchen(String name) {   // sucht JavaLogik.Termin nach Namen -- aber nur erster gefundener
        List<Termin> gefundeneTermine = new ArrayList<>();
        for (Termin termin : termine) {
            if (termin.getTitel().equals(name)) {
                gefundeneTermine.add(termin);
            }
        }
//        if (gefundeneTermine.isEmpty()) {
//            return null;    // falls kein JavaLogik.Termin gefunden wurde -- vllt anders lösen
//        }
        return gefundeneTermine;
    }

    public boolean terminErstellenUndHinzufuegen(String titel, Instant start, Instant ende, String beschreibung, Kategorie kategorie) {
        Termin termin = new Termin(titel, start, ende, beschreibung, kategorie);
        if (konflikt(termin)) {
            // fragen ob trotzdem hinzufügen
            if (/*GUI.trotzdemhinzufuegen()*/true) {   // Methode die eine Abfrage in der GUI darstellt -- und noch geschrieben werden muss
                terminErstellenUndHinzufuegen(termin);  // wenn der JavaLogik.Benutzer trotz Konflikt hinzufügen will
                return true;
            } else {
                return false; // JavaLogik.Termin wird nicht hinzugefügt
            }
        } else {
            terminErstellenUndHinzufuegen(termin);
            return true;
        }
    }

    public boolean kategorieDoppelt(String name) {
        for (Kategorie kategorie : kategorien) {
            if (kategorie.getName().equals(name)) {
                //GUI.zeigeFehlermeldung("JavaLogik.Kategorie existiert bereits!");  Methode die eine Fehlermeldung in der GUI darstellt -- und noch geschrieben werden muss
                return true; // JavaLogik.Kategorie existiert bereits
            }
        }
        return false;
    }

    public boolean erstelleKategorie(String name, String farbe) {
        Kategorie kategorie = new Kategorie(name, farbe);  // Farbe durch auswahl, und dann als hex code speichern
        if (!kategorieDoppelt(name)) {
            kategorieHinzufuegen(kategorie);
            return true;
        } else {
            return false;
        }
        // Kategorieninformationen müssen aus der GUI kommen
    }

    public boolean terminBearbeiten(Termin termin, String neuerTitel, Instant neuerStart, Instant neuesEnde, String neueBeschreibung, Kategorie neueKategorie) {
        if (konflikt(new Termin(neuerTitel, neuerStart, neuesEnde, neueBeschreibung, neueKategorie))) {
            // fragen ob trotzdem bearbeiten
            if (/*!GUI.trotzdemBearbeiten()*/true) {   // Methode die eine Abfrage in der GUI darstellt -- und noch geschrieben werden muss
                return false; // JavaLogik.Termin wird nicht bearbeitet
            }
        }
        termin.setTitel(neuerTitel);
        termin.setDatum(neuerStart);
        termin.setEnde(neuesEnde);
        termin.setBeschreibung(neueBeschreibung);
        termin.setKategorie(neueKategorie);
        return true;
        // Neue Termininformationen müssen aus der GUI kommen
    }
}


