import java.util.ArrayList;
import java.util.List;

public class Familie {
    private String familienName;
    private List<Benutzer> mitglieder;

    public Familie(String familienName) {
        this.familienName = familienName;
        this.mitglieder = new ArrayList<Benutzer>();
    }

    public String getFamilienName() {
        return familienName;
    }

    public List<Benutzer> getMitglieder() {    // eventuell noch ausgaben methode der mitglieder
        return mitglieder;
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


}
