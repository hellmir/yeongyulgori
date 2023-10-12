package personal.yeongyulgori.user.model.dto;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
public class SignInResponseDto {

    private String username;
    private Collection<? extends GrantedAuthority> roles;

    private SignInResponseDto(String username, Collection<? extends GrantedAuthority> roles) {
        this.username = username;
        this.roles = roles;
    }

    public static SignInResponseDto of(String username, Collection<? extends GrantedAuthority> roles) {
        return new SignInResponseDto(username, roles);
    }

}
