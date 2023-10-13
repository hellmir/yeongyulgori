package personal.yeongyulgori.user.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PasswordRequestDto {

    @ApiModelProperty(value = "비밀번호", example = "1234")
    private String password;

    public PasswordRequestDto(String password) {
        this.password = password;
    }

}
