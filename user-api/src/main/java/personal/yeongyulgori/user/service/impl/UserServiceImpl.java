package personal.yeongyulgori.user.service.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import personal.yeongyulgori.user.model.dto.UserResponseDto;
import personal.yeongyulgori.user.model.entity.User;
import personal.yeongyulgori.user.model.repository.UserRepository;
import personal.yeongyulgori.user.service.UserService;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

/**
 * 회원, 비회원 공용 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true, timeout = 10)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponseDto getUserProfile(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("해당 회원이 존재하지 않습니다. username: " + username));

        return UserResponseDto.of(user.getEmail(), user.getUsername(), user.getFullName(),
                user.getRoles(), user.getCreatedAt(), user.getModifiedAt());

    }

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true, timeout = 30)
    public Page<UserResponseDto> getSearchedUsers(String keyword, Pageable pageable) {

        Page<User> users = userRepository.findByFullNameContaining(keyword, pageable);

        List<UserResponseDto> userResponseDtos = users.getContent().stream()
                .map(user -> UserResponseDto.of(user.getEmail(), user.getUsername(), user.getFullName(),
                        user.getRoles(), user.getCreatedAt(), user.getModifiedAt()))
                .collect(Collectors.toList());

        return new PageImpl<>(userResponseDtos, pageable, users.getTotalElements());

    }

}
