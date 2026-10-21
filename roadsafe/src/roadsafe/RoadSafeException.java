package roadsafe;

// our own checked exception so GUI only needs to catch one type
public class RoadSafeException extends Exception {

    public RoadSafeException(String message) {
        super(message);
    }

    // for wrapping other exceptions like SQLException
    public RoadSafeException(String message, Throwable cause) {
        super(message, cause);
    }
}
