package roadsafe.util;
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
