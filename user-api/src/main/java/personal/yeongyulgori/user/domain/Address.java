package personal.yeongyulgori.user.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotBlank;

@Embeddable
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Address {

    @NotBlank(message = "지역 정보는 필수값입니다.")
    private String city;

    @NotBlank(message = "도로명은 필수값입니다.")
    private String street;

    @NotBlank(message = "우편번호는 필수값입니다.")
    private String zipcode;

    @NotBlank(message = "상세 주소는 필수값입니다.")
    private String detailedAddress;

}