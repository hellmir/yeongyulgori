package personal.yeongyulgori.user.model.dto;

import lombok.Getter;
import personal.yeongyulgori.user.model.constant.Role;

import java.util.List;

@Getter
public class SignInResponseDto {

    private String username;
    private List<Role> roles;

    private SignInResponseDto(String username, List<Role> roles) {
        this.username = username;
        this.roles = roles;
    }

    public static SignInResponseDto of(String username, List<Role> roles) {
        return new SignInResponseDto(username, roles);
    }

}
