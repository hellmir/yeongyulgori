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

    @ApiModelProperty(value = "성명", example = "고길동")
    private String name;

    private Address address;

    @ApiModelProperty(value = "분류", example = "BUSINESS_USER")
    private Role role;

    @ApiModelProperty(value = "Base64로 인코딩된 프로필 이미지 데이터 URI", example = "NewEncodedDataURI")
    private String profileImage;

}
