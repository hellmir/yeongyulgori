package personal.yeongyulgori.user.client.mailgun;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class SendMailForm {

    private String from;
    private String to;
    private String subject;
    private String text;

}
