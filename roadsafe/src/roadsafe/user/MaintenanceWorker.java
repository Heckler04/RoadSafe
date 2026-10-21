package roadsafe.user;

// worker can only update status of hazards assigned to them
public class MaintenanceWorker extends User {

    public MaintenanceWorker(int id, String username, String fullName) {
        super(id, username, fullName);
    }

    public String getRole() {
        return WORKER;
    }

    public boolean canUpdateStatus() {
        return true;
    }
}
