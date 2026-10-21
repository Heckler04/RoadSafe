package roadsafe.hazard;
import java.sql.SQLException;
import java.util.Vector;
import roadsafe.database.DatabaseManager;
import roadsafe.RoadSafeException;
import roadsafe.user.User;
import roadsafe.user.Notifiable;
import roadsafe.user.Reportable;
public class HazardManager {
    private final HazardRepository repository;
    public HazardManager() {
        this(DatabaseManager.getInstance());
    }
    public HazardManager(HazardRepository repository) {
        this.repository = repository;
    }

    public void reportHazard(User user, Hazard hazard)
            throws RoadSafeException {
        if (!user.canReport()) {
            throw new RoadSafeException("You are not allowed to submit reports.");
        }
        if (user instanceof Reportable) {
            Reportable r = (Reportable) user;
            if (!r.canSubmitReport()) {
                throw new RoadSafeException("You are not allowed to submit reports.");
            }
        }
        try {
            repository.addHazard(hazard);
        } catch (SQLException e) {
            throw wrap(e);
        }
    }
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
            if (user.getRole().equals(User.WORKER)
                    && !isAssignedTo(user, id)) {
                throw new RoadSafeException(
                        "This hazard is not assigned to you.");
            }
            repository.updateStatus(id, status);
            String reporter = DatabaseManager.getInstance().getReporterOfHazard(id);
            if (reporter != null) {
                DatabaseManager.getInstance().addNotification(reporter,
                        "Status of your hazard #" + id + " changed to: " + status);
            }
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
            DatabaseManager.getInstance().addNotification(workerUsername,
                    "Hazard #" + id + " has been assigned to you.");
            String reporter = DatabaseManager.getInstance().getReporterOfHazard(id);
            if (reporter != null && !reporter.equals(workerUsername)) {
                DatabaseManager.getInstance().addNotification(reporter,
                        "Your hazard #" + id + " was assigned to worker " + workerUsername + ".");
            }
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
    public void editHazard(User user, int id, String hazardType,
                           String location, String description, String severity)
            throws RoadSafeException {
        if (!user.canUpdateStatus() && !user.canDelete()) {
            throw new RoadSafeException("You are not allowed to edit reports.");
        }
        try {
            if (user.getRole().equals(User.WORKER)
                    && !isAssignedTo(user, id)) {
                throw new RoadSafeException(
                        "This hazard is not assigned to you.");
            }
            repository.updateHazard(id, hazardType, location, description, severity);
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
