package personal.yeongyulgori.post.exception.serious.sub;

import org.springframework.http.HttpStatus;
import personal.yeongyulgori.post.exception.serious.AbstractSeriousException;

public class InconsistentUserException extends AbstractSeriousException {

    public InconsistentUserException(String message) {
        super(message);
    }

    @Override
    public int getStatusCode() {
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

}
