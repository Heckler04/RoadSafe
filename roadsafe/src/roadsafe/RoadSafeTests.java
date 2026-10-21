package roadsafe;

import java.sql.SQLException;
import java.util.Vector;
import roadsafe.hazard.Hazard;
import roadsafe.hazard.HazardManager;
import roadsafe.hazard.HazardRepository;
import roadsafe.user.Admin;
import roadsafe.user.Citizen;
import roadsafe.user.MaintenanceWorker;
import roadsafe.user.User;
import roadsafe.util.PasswordUtil;
import roadsafe.util.Validator;

// simple tests (no JUnit)
// uses in-memory repo so no real database needed
public class RoadSafeTests {

    private static int passed = 0;
    private static int failed = 0;

    // fake repository using Vector (syllabus collection)
    static class InMemoryRepository implements HazardRepository {
        private final Vector data = new Vector();
        private int nextId = 1;

        public void addHazard(Hazard h) {
            data.add(new Hazard(nextId, h.getCitizenName(), h.getReportedBy(),
                    h.getHazardType(), h.getLocation(), h.getDescription(),
                    h.getSeverity(), "Reported", "Unassigned"));
            nextId++;
        }

        public Vector getAllHazards() {
            return new Vector(data);
        }

        public Vector getHazardsByReporter(String username) {
            Vector result = new Vector();
            for (int i = 0; i < data.size(); i++) {
                if (username.equals(((Hazard)data.get(i)).getReportedBy())) {
                    result.add(data.get(i));
                }
            }
            return result;
        }

        public Vector getHazardsByWorker(String username) {
            Vector result = new Vector();
            for (int i = 0; i < data.size(); i++) {
                if (username.equals(((Hazard)data.get(i)).getAssignedWorker())) {
                    result.add(data.get(i));
                }
            }
            return result;
        }

        private Hazard find(int id) throws SQLException {
            for (int i = 0; i < data.size(); i++) {
                if (((Hazard)data.get(i)).getHazardId() == id) {
                    return (Hazard) data.get(i);
                }
            }
            throw new SQLException("Hazard ID not found.");
        }

        public void updateStatus(int id, String status) throws SQLException {
            find(id).updateStatus(status);
        }

        public void assignWorker(int id, String worker) throws SQLException {
            find(id).setAssignedWorker(worker);
        }

        public void deleteHazard(int id) throws SQLException {
            data.remove(find(id));
        }
    }

    static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("  PASS  " + name);
        } else {
            failed++;
            System.out.println("  FAIL  " + name);
        }
    }

    static Hazard newHazard(String reportedBy) {
        return new Hazard(0, "Name", reportedBy, "Pothole", "MG Road",
                "Big pothole near the bus stop", "High",
                "Reported", "Unassigned");
    }

    static boolean canReport(HazardManager m, User u, Hazard h) {
        try {
            m.reportHazard(u, h);
            return true;
        } catch (RoadSafeException e) {
            return false;
        }
    }

    static boolean canDelete(HazardManager m, User u, int id) {
        try {
            m.deleteHazard(u, id);
            return true;
        } catch (RoadSafeException e) {
            return false;
        }
    }

    static boolean canAssign(HazardManager m, User u, int id, String worker) {
        try {
            m.assignWorker(u, id, worker);
            return true;
        } catch (RoadSafeException e) {
            return false;
        }
    }

    static boolean canUpdate(HazardManager m, User u, int id, String status) {
        try {
            m.updateStatus(u, id, status);
            return true;
        } catch (RoadSafeException e) {
            return false;
        }
    }

    static int visibleCount(HazardManager m, User u) {
        try {
            return m.getVisibleHazards(u).size();
        } catch (RoadSafeException e) {
            return -1;
        }
    }

    static String firstStatus(HazardManager m, User u) {
        try {
            Vector list = m.getVisibleHazards(u);
            if (list.size() == 0) {
                return "";
            }
            return ((Hazard)list.get(0)).getStatus();
        } catch (RoadSafeException e) {
            return "";
        }
    }

    public static void main(String[] args) {
        User admin = new Admin(1, "admin", "Admin");
        User worker = new MaintenanceWorker(2, "worker1", "Ravi");
        User worker2 = new MaintenanceWorker(3, "worker2", "Anita");
        User citizen = new Citizen(4, "citizen", "Demo Citizen");
        User other = new Citizen(5, "other", "Other Citizen");

        System.out.println("Password check");
        check("correct password matches",
                PasswordUtil.matches("secret1", "secret1"));
        check("wrong password rejected",
                !PasswordUtil.matches("wrong", "secret1"));

        System.out.println("\nValidator");
        check("empty name rejected", Validator.fullName("") != null);
        check("short name rejected", Validator.fullName("A") != null);
        check("good name accepted", Validator.fullName("Ravi Kumar") == null);
        check("empty username rejected", Validator.username("") != null);
        check("short password rejected", Validator.password("abc") != null);
        check("good password accepted", Validator.password("secret1") == null);
        check("short location rejected", Validator.location("ab") != null);
        check("good location accepted", Validator.location("MG Road") == null);

        System.out.println("\nPermissions and visibility");
        HazardManager m = new HazardManager(new InMemoryRepository());

        check("citizen can report", canReport(m, citizen, newHazard("citizen")));
        check("admin can report", canReport(m, admin, newHazard("admin")));
        check("worker cannot report", !canReport(m, worker, newHazard("worker1")));

        check("admin sees 2 reports", visibleCount(m, admin) == 2);
        check("citizen sees 1 report", visibleCount(m, citizen) == 1);
        check("other citizen sees 0", visibleCount(m, other) == 0);
        check("worker sees 0 (not assigned)", visibleCount(m, worker) == 0);

        check("admin can assign", canAssign(m, admin, 1, "worker1"));
        check("worker can update own", canUpdate(m, worker, 1, "In Progress"));
        check("worker cannot update other",
                !canUpdate(m, worker2, 1, "Resolved"));
        check("citizen cannot update",
                !canUpdate(m, citizen, 1, "Resolved"));
        check("status became In Progress",
                firstStatus(m, worker).equals("In Progress"));

        check("citizen cannot delete", !canDelete(m, citizen, 1));
        check("admin can delete", canDelete(m, admin, 1));
        check("admin now sees 1 report", visibleCount(m, admin) == 1);

        System.out.println("\n----- Summary -----");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        if (failed == 0) {
            System.out.println("All tests passed.");
        } else {
            System.out.println("Some tests failed.");
        }
    }
}
