package personal.yeongyulgori.user.client;

import feign.Response;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.PostMapping;
import personal.yeongyulgori.user.client.mailgun.EmailForm;

@FeignClient(name = "mailgun", url = "https://api.mailgun.net/v3/")
@Qualifier("mailgun")
public interface MailgunClient {

    @PostMapping("sandboxb3a3c50e51c44354a6ecab215e08251b.mailgun.org/messages")
    Response sendEmail(@SpringQueryMap EmailForm emailForm);

}
