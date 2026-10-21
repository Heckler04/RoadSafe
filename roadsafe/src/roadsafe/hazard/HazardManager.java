package roadsafe.hazard;

import java.sql.SQLException;
import java.util.Vector;
import roadsafe.database.DatabaseManager;
import roadsafe.RoadSafeException;
import roadsafe.user.User;

// business rules - who can do what
// uses HazardRepository so it works with real db or test fake
public class HazardManager {

    private final HazardRepository repository;

    // normal use - real database
    public HazardManager() {
        this(DatabaseManager.getInstance());
    }

    // for tests - pass in whatever repo you want
    public HazardManager(HazardRepository repository) {
        this.repository = repository;
    }

    public void reportHazard(User user, Hazard hazard)
            throws RoadSafeException {
        if (!user.canReport()) {
            throw new RoadSafeException("You are not allowed to submit reports.");
        }
        try {
            repository.addHazard(hazard);
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    // only return what this user is allowed to see
    public Vector getVisibleHazards(User user)
            throws RoadSafeException {
        try {
            String role = user.getRole();
            if (role.equals(User.ADMIN)) {
                return repository.getAllHazards();
            } else if (role.equals(User.WORKER)) {
                return repository.getHazardsByWorker(user.getUsername());
            } else {
                return repository.getHazardsByReporter(user.getUsername());
            }
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    public void updateStatus(User user, int id, String status)
            throws RoadSafeException {
        if (!user.canUpdateStatus()) {
            throw new RoadSafeException("You are not allowed to update status.");
        }
        if (!Hazard.isValidStatus(status)) {
            throw new RoadSafeException("Invalid status: " + status);
        }
        try {
            // workers can only touch their own assigned hazards
            if (user.getRole().equals(User.WORKER)
                    && !isAssignedTo(user, id)) {
                throw new RoadSafeException(
                        "This hazard is not assigned to you.");
            }
            repository.updateStatus(id, status);
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    public void assignWorker(User user, int id, String workerUsername)
            throws RoadSafeException {
        if (!user.canAssignWorkers()) {
            throw new RoadSafeException("Only an Admin can assign workers.");
        }
        if (workerUsername == null || workerUsername.trim().isEmpty()) {
            throw new RoadSafeException("Select a worker first.");
        }
        try {
            repository.assignWorker(id, workerUsername);
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    public void deleteHazard(User user, int id) throws RoadSafeException {
        if (!user.canDelete()) {
            throw new RoadSafeException("Only an Admin can delete reports.");
        }
        try {
            repository.deleteHazard(id);
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    private boolean isAssignedTo(User worker, int hazardId)
            throws SQLException {
        Vector assigned =
                repository.getHazardsByWorker(worker.getUsername());
        for (int i = 0; i < assigned.size(); i++) {
            if (((Hazard)assigned.get(i)).getHazardId() == hazardId) {
                return true;
            }
        }
        return false;
    }

    private RoadSafeException wrap(SQLException e) {
        return new RoadSafeException(e.getMessage(), e);
    }
}
