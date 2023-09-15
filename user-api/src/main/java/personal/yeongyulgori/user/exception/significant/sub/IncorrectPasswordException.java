package personal.yeongyulgori.user.exception.significant.sub;

import org.springframework.http.HttpStatus;
import personal.yeongyulgori.user.exception.significant.AbstractSignificantException;

public class IncorrectPasswordException extends AbstractSignificantException {

    public IncorrectPasswordException() {
        super("비밀번호가 일치하지 않습니다.");
    }

    @Override
    public int getStatusCode() {
        return HttpStatus.UNAUTHORIZED.value();
    }

}