package personal.yeongyulgori.user.exception.general.sub;

import org.springframework.http.HttpStatus;
import personal.yeongyulgori.user.exception.general.AbstractGeneralException;

public class DuplicateUsernameException extends AbstractGeneralException {

    public DuplicateUsernameException(String message) {
        super(message);
    }

    @Override
    public int getStatusCode() {
        return HttpStatus.CONFLICT.value();
    }

}
