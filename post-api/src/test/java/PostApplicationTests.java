import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import personal.yeongyulgori.post.PostApplication;

@TestPropertySource("classpath:application-test.properties")
@ContextConfiguration(classes = PostApplication.class)
public class PostApplicationTests {

    @Test
    void contextLoads() {
    }

}
