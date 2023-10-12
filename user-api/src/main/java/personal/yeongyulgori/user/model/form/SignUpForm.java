package personal.yeongyulgori.user.model.form;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import personal.yeongyulgori.user.model.constant.Role;
import personal.yeongyulgori.user.model.entity.embedment.Address;
import personal.yeongyulgori.user.validation.group.OnSignUp;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SignUpForm {

    @ApiModelProperty(value = "이메일 주소", example = "abcd@abc.com")
    @NotBlank(groups = OnSignUp.class, message = "이메일 주소는 필수값입니다.")
    @Email(groups = OnSignUp.class, message = "이메일 주소 형식이 잘못되었습니다. 예: abcd@abc.com")
    private String email;

    @ApiModelProperty(value = "사용자 이름", example = "gildong1234")
    @NotBlank(groups = OnSignUp.class, message = "사용자 이름은 필수값입니다.")
    @Pattern(groups = OnSignUp.class, regexp = "^[a-z0-9]{4,16}$",
            message = "사용자 이름은 4~16자 이하의 영소문자와 숫자만으로 구성되어야 합니다. 예: gildong1234")
    private String username;

    @ApiModelProperty(value = "비밀번호", example = "1234")
    @NotBlank(groups = OnSignUp.class, message = "비밀번호는 필수값입니다.")
    private String password;

    @ApiModelProperty(value = "성명", example = "홍길동")
    @NotBlank(groups = OnSignUp.class, message = "성명은 필수값입니다.")
    private String fullName;

    @ApiModelProperty(value = "생일", example = "2000-01-01")
    @NotNull(groups = OnSignUp.class, message = "생일은 필수값입니다.")
    private LocalDate birthDate;

    @ApiModelProperty(value = "휴대폰 번호", example = "01012345678")
    @Pattern(groups = OnSignUp.class, regexp = "^010\\d{8}$", message = "전화번호 형식이 잘못되었습니다. 예: 01012345678")
    private String phoneNumber;

    @Valid
    private Address address;

    @ApiModelProperty(value = "분류", example = "[ROLE_GENERAL_USER, ROLE_BUSINESS_USER]")
    @NotNull(groups = OnSignUp.class, message = "회원 분류를 선택해 주세요.")
    private List<Role> roles;

    @ApiModelProperty(value = "Base64로 인코딩된 프로필 이미지 데이터 URI", example = "YourEncodedDataURI")
    private String profileImage;

}
