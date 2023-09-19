package personal.yeongyulgori.post.exception.general;

public abstract class AbstractGeneralException extends RuntimeException {

    public AbstractGeneralException(String message) {
        super(message);
    }

    abstract public int getStatusCode();

}
