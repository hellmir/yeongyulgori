package personal.yeongyulgori.user.model.form;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import personal.yeongyulgori.user.model.constant.Role;
import personal.yeongyulgori.user.model.entity.embedment.Address;

import java.util.List;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class InformationUpdateForm {

    @ApiModelProperty(value = "id", example = "1")
    private Long id;

    @ApiModelProperty(value = "성명", example = "고길동")
    private String fullName;

    private Address address;

    @ApiModelProperty(value = "권한", example = "ROLE_BUSINESS_USER")
    private List<Role> roles;

    @ApiModelProperty(value = "Base64로 인코딩된 프로필 이미지 데이터 URI", example = "NewEncodedDataURI")
    private String profileImage;

}
