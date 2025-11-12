import java.time.Instant;

public class Termin {

    private static int nextId = 1;
    private final int id;
    private String titel;
    private Instant start;    // Instant zählt Zeit seit 1970
    private Instant ende;
    private String beschreibung;
    private Kategorie kategorie;

    public Termin(String titel, Instant start, Instant ende, String beschreibung, Kategorie kategorie) {
        this.id = nextId++;
        this.titel = titel;
        this.start = start;
        this.ende = ende;
        this.beschreibung = beschreibung;
        this.kategorie = kategorie;  // gerade nur eine Kategorie pro Termin - vllt. ändern
    }

    public int getId() {
        return id;
    }

    public String getTitel() {
        return titel;
    }

    public Instant getStart() {
        return start;
    }

    public Instant getEnde() {
        return ende;
    }

    public String getBeschreibung() {
        return beschreibung;
    }

    public Kategorie getKategorie() {
        return kategorie;
    }

    public void setTitel(String titel) {
        this.titel = titel;
    }

    public void setDatum(Instant start) {
        this.start = start;
    }

    public void setEnde(Instant ende) {
        this.ende = ende;
    }

    public void setBeschreibung(String beschreibung) {
    }
}

