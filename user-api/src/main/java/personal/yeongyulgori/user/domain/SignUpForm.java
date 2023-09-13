package personal.yeongyulgori.user.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import personal.yeongyulgori.user.constant.Role;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SignUpForm {

    @ApiModelProperty(value = "이메일 주소", example = "abcd@abc.com")
    @NotBlank(message = "이메일 주소는 필수값입니다.")
    @Email(message = "이메일 주소 형식이 잘못되었습니다.")
    private String email;

    @ApiModelProperty(value = "사용자 이름", example = "gildong1234")
    @NotBlank(message = "사용자 이름은 필수값입니다.")
    private String username;

    @ApiModelProperty(value = "비밀번호", example = "1234")
    @NotBlank(message = "비밀번호는 필수값입니다.")
    private String password;

    @ApiModelProperty(value = "성명", example = "홍길동")
    @NotBlank(message = "성명은 필수값입니다.")
    private String name;

    @ApiModelProperty(value = "생일", example = "2000-01-01")
    @NotBlank(message = "생일은 필수값입니다.")
    private LocalDate birthDate;

    @ApiModelProperty(value = "휴대폰 번호", example = "01012345678")
    @Pattern(regexp = "^010\\d{8}$", message = "전화번호 형식이 잘못되었습니다. 예: 01012345678")
    private String phoneNumber;

    private Address address;

    @ApiModelProperty(value = "분류", example = "GENERAL_USER")
    @NotBlank(message = "회원 분류를 선택해 주세요.")
    private Role role;

    @ApiModelProperty(value = "프로필 이미지 파일")
    private MultipartFile profileImage;

}
