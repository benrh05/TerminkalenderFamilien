public class MainLogik {

    public static void main(String[] args) {
        // was auch immer hier reinkommt, denke mal gui start und so
    }

    public static void erstelleFamilie(String familienName) {
        Familie familie = new Familie(familienName);
        // Familienname muss aus der GUI kommen
    }

    public static void erstelleBenutzer(Familie familie, String name, String email, String password, String rolle) {
        Benutzer benutzer = new Benutzer(name, email, password, rolle);
        familie.benutzerHinzufuegen(benutzer);
        // Benutzerinformationen müssen aus der GUI kommen
    }
}

