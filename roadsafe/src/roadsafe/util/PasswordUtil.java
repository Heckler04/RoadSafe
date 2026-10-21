package roadsafe.util;

// simple helper for password check
// syllabus does not cover hashing libraries, so we keep it plain
public class PasswordUtil {

    private PasswordUtil() {
    }

    // just compare the two strings
    public static boolean matches(String typed, String stored) {
        if (typed == null || stored == null) {
            return false;
        }
        return typed.equals(stored);
    }
}
