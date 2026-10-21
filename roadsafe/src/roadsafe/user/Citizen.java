package roadsafe.user;

// normal citizen - can only report hazards
public class Citizen extends User {

    public Citizen(int id, String username, String fullName) {
        super(id, username, fullName);
    }

    public String getRole() {
        return CITIZEN;
    }

    public boolean canReport() {
        return true;
    }
    // other permissions stay false from parent
}
