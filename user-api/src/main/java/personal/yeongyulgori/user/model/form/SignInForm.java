package personal.yeongyulgori.user.model.form;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SignInForm {

    @ApiModelProperty(value = "email or username", example = "abcd@abc.com")
    private String emailOrUsername;

    @ApiModelProperty(value = "password", example = "1234")
    @NotBlank(message = "비밀번호는 필수값입니다.")
    private String password;

}
