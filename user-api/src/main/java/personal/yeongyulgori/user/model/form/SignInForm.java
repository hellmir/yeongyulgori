package personal.yeongyulgori.user.model.form;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SignInForm {

    @ApiModelProperty(value = "email", example = "abcd@abc.com")
    @Email(message = "이메일 주소 형식이 잘못되었습니다.")
    private String email;

    @ApiModelProperty(value = "username", example = "gildong1234")
    private String username;

    @ApiModelProperty(value = "password", example = "1234")
    @NotBlank(message = "비밀번호는 필수값입니다.")
    private String password;

}
