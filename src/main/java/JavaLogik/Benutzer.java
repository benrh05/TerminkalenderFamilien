package JavaLogik;

public class Benutzer {

    private static int nextId = 1;
    private int id;
    private String name;
    private String email;
    private String password;
    private String rolle;
    private Kalender kalender;

    public Benutzer(String name, String email, String password, String rolle) {
        this.id = nextId++;
        this.name = name;
        this.email = email;
        this.password = password;
        this.rolle = rolle;
        this.kalender = new Kalender();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getRolle() {
        return rolle;
    }

    public Kalender getKalender() {
        return kalender;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRolle(String rolle) {
        this.rolle = rolle;
    }


}

