package personal.yeongyulgori.user.exception.significant;

public abstract class AbstractSignificantException extends RuntimeException {

    public AbstractSignificantException(String message) {
        super(message);
    }

    abstract public int getStatusCode();

}
