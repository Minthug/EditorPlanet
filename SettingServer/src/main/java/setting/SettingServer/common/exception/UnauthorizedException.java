package setting.SettingServer.common.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnauthorizedException(String message) {
        super(message);
    }
}
