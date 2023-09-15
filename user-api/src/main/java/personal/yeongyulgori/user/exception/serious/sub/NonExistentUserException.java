package personal.yeongyulgori.user.exception.serious.sub;

import org.springframework.http.HttpStatus;
import personal.yeongyulgori.user.exception.serious.AbstractSeriousException;

public class NonExistentUserException extends AbstractSeriousException {

    public NonExistentUserException(String message) {
        super(message);
    }

    @Override
    public int getStatusCode() {
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

}
