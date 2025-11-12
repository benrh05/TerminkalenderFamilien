public class Kategorie {

    private static int nextId = 1;
    private final int id;
    private String name;
    private String farbe;

    public Kategorie(String name, String farbe) {
        this.id = nextId++;
        this.name = name;
        this.farbe = farbe;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getFarbe() {
        return farbe;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFarbe(String farbe) {
        this.farbe = farbe;
    }
}
