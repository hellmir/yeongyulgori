package personal.yeongyulgori.user.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import personal.yeongyulgori.user.dto.UserResponseDto;

public interface UserService {

    UserResponseDto getUserProfile(String username);

    Page<UserResponseDto> getSearchedUsers(String name, Pageable pageable);

}
