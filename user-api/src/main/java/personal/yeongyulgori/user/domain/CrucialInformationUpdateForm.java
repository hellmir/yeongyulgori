package personal.yeongyulgori.user.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CrucialInformationUpdateForm {

    @ApiModelProperty(value = "id", example = "1")
    private Long id;

    @ApiModelProperty(value = "이메일 주소", example = "abcd@abc.com")
    private String email;

    @ApiModelProperty(value = "비밀번호", example = "1234")
    private String password;

    @ApiModelProperty(value = "휴대폰 번호", example = "01012345678")
    private String phoneNumber;

}
