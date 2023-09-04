import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import personal.yeongyulgori.user.UserApplication;

@TestPropertySource("classpath:application-test.properties")
@ContextConfiguration(classes = UserApplication.class)
public class UserApplicationTests {

    @Test
    void contextLoads() {
    }

}
