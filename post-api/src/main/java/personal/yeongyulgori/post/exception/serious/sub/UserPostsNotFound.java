package personal.yeongyulgori.post.exception.serious.sub;

import org.springframework.http.HttpStatus;
import personal.yeongyulgori.post.exception.serious.AbstractSeriousException;

public class UserPostsNotFound extends AbstractSeriousException {

    public UserPostsNotFound(String message) {
        super(message);
    }

    @Override
    public int getStatusCode() {
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

}
