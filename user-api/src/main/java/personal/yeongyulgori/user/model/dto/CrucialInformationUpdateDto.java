package personal.yeongyulgori.user.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CrucialInformationUpdateDto {

    @ApiModelProperty(value = "id", example = "1")
    private Long id;

    @ApiModelProperty(value = "현재 비밀번호", example = "1234")
    private String password;

    @ApiModelProperty(value = "변경할 비밀번호", example = "12345")
    private String newPassword;

    @ApiModelProperty(value = "변경할 이메일 주소", example = "abcd@abc.com")
    @Email(message = "이메일 주소 형식이 잘못되었습니다. 예: abcd@abc.com")
    private String email;

    @ApiModelProperty(value = "변경할 휴대폰 번호", example = "01012345678")
    @Pattern(regexp = "^010\\d{8}$", message = "전화번호 형식이 잘못되었습니다. 예: 01012345678")
    private String phoneNumber;

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

}
