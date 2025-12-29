package JavaLogik;

import java.util.ArrayList;
import java.util.List;

public class Familie {
    private String familienName;
    private List<Benutzer> mitglieder;

    // neu: Kategorien auf Familien-Ebene
    private List<Kategorie> kategorien;

    public Familie(String familienName) {
        this.familienName = familienName;
        this.mitglieder = new ArrayList<Benutzer>();
        this.kategorien = new ArrayList<Kategorie>();
    }

    public String getFamilienName() {
        return familienName;
    }

    public List<Benutzer> getMitglieder() {    // eventuell noch ausgaben methode der mitglieder
        return mitglieder;
    }

    public boolean benutzernameExistiert(String name) {
        for (Benutzer benutzer : mitglieder) {
            if (benutzer.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean erstelleBenutzer(String name, String email, String password, String rolle) {
        if (benutzernameExistiert(name)) {
            return false; // Name existiert bereits
        }
        Benutzer benutzer = new Benutzer(name, email, password, rolle);
        benutzerHinzufuegen(benutzer);
        return true;
    }

    public void benutzerHinzufuegen(Benutzer benutzer) {
        this.mitglieder.add(benutzer);
    }

    public List<String> getBenutzerNamen() {
        List<String> namen = new ArrayList<>();
        for (Benutzer benutzer : mitglieder) {
            namen.add(benutzer.getName());
        }
        return namen;
    }

    public List<Kategorie> getKategorien() {
        return kategorien;
    }

    public List<String> getKategorienNamen() {
        List<String> namen = new ArrayList<>();
        for (Kategorie k : kategorien) {
            namen.add(k.getName());
        }
        return namen;
    }

    public void kategorieHinzufuegen(Kategorie kategorie) {
        if (kategorie == null) return;
        if (!kategorieDoppelt(kategorie.getName())) {
            kategorien.add(kategorie);
        }
    }

    public Kategorie getKategorieByName(String name) {
        if (name == null) return null;
        for (Kategorie k : kategorien) {
            if (name.equals(k.getName())) return k;
        }
        return null;
    }

    public boolean kategorieDoppelt(String name) {
        if (name == null) return false;
        for (Kategorie k : kategorien) {
            if (k.getName() != null && k.getName().equals(name)) return true;
        }
        return false;
    }

    public boolean erstelleKategorie(String name, String farbe) {
        if (name == null || name.isBlank()) return false;
        if (kategorieDoppelt(name)) return false;
        Kategorie kategorie = new Kategorie(name, farbe);
        kategorien.add(kategorie);
        return true;
    }

}
