import java.util.ArrayList;
import java.util.List;

public class Familie {
    private String familienName;
    private List<Benutzer> mitglieder;

    public Familie(String familienName) {
        this.familienName = familienName;
        this.mitglieder = new ArrayList<Benutzer>();
    }

    public static Familie erstelleFamilie(String familienName) {
        return new Familie(familienName);
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
            GUI.fehlermeldungAnzeigen(); // muss noch in gui geschrieben werden
            return false; // Name existiert bereits
        }
        Benutzer benutzer = new Benutzer(name, email, password, rolle);
        benutzerHinzufuegen(benutzer);
        return true;
    }

    public void benutzerHinzufuegen(Benutzer benutzer) {
        this.mitglieder.add(benutzer);
    }

    public void benutzerLoeschen(Benutzer benutzer) {
        this.mitglieder.remove(benutzer);
    }

    public void setFamilienName(String familienName) {    // eventuell wegmachen und final machen
        this.familienName = familienName;
    }

    public boolean benutzerBearbeiten(Benutzer benutzer, String neuerName, String neueEmail, String neuesPasswort, String neueRolle) {
        if (benutzernameExistiert(neuerName)) {
            GUI.fehlermeldungAnzeigen(); // muss noch in gui geschrieben werden
            return false; // Name existiert bereits
        }
        benutzer.setName(neuerName);
        benutzer.setEmail(neueEmail);
        benutzer.setPassword(neuesPasswort);
        benutzer.setRolle(neueRolle);
        return true;
    }


}
