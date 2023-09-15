package personal.yeongyulgori.user.exception.general.sub;

import org.springframework.http.HttpStatus;
import personal.yeongyulgori.user.exception.general.AbstractGeneralException;

public class DuplicateUserException extends AbstractGeneralException {

    public DuplicateUserException(String message) {
        super(message);
    }

    @Override
    public int getStatusCode() {
        return HttpStatus.CONFLICT.value();
    }

}
