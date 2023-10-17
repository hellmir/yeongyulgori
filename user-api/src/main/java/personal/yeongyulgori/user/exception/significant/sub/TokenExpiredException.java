package personal.yeongyulgori.user.exception.significant.sub;

import org.springframework.http.HttpStatus;
import personal.yeongyulgori.user.exception.significant.AbstractSignificantException;

public class TokenExpiredException extends AbstractSignificantException {

    public TokenExpiredException() {
        super("토큰이 만료되었습니다. 비밀번호를 재설정하려면 새로운 토큰을 발급 받아야 합니다.");
    }

    @Override
    public int getStatusCode() {
        return HttpStatus.UNAUTHORIZED.value();
    }

}
