package setting.SettingServer.common.exception;

public class DuplicateRatingException extends RuntimeException {

    public DuplicateRatingException(String message) {
        super(message);
    }

    public DuplicateRatingException(String message, Throwable cause) {
        super(message, cause);
    }
}
