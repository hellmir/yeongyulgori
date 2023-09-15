package personal.yeongyulgori.user.exception.serious;

public abstract class AbstractSeriousException extends RuntimeException {

    public AbstractSeriousException(String message) {
        super(message);
    }

    abstract public int getStatusCode();

}
