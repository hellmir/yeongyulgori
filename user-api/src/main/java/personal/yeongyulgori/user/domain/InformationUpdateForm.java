package personal.yeongyulgori.user.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import personal.yeongyulgori.user.constant.Role;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class InformationUpdateForm {

    @ApiModelProperty(value = "id", example = "1")
    private Long id;

    @ApiModelProperty(value = "성명", example = "홍길동")
    private String name;

    @ApiModelProperty(value = "주소", example = "2000-01-01")
    private Address address;

    @ApiModelProperty(value = "분류", example = "GENERAL_USER")
    private Role role;

    @ApiModelProperty(value = "프로필 이미지 파일")
    private String profileImage;

}
