package processing;

public class InvalidReadException extends RuntimeException {

    public InvalidReadException(String message) {
        super(message);
    }
}