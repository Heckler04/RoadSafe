package roadsafe.user;

// admin can do everything
public class Admin extends User {

    public Admin(int id, String username, String fullName) {
        super(id, username, fullName);
    }

    public String getRole() {
        return ADMIN;
    }

    public boolean canReport() {
        return true;
    }

    public boolean canUpdateStatus() {
        return true;
    }

    public boolean canAssignWorkers() {
        return true;
    }

    public boolean canDelete() {
        return true;
    }
}
