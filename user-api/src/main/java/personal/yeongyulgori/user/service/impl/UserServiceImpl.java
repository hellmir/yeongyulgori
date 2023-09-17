package personal.yeongyulgori.user.service.impl;


import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import personal.yeongyulgori.user.domain.model.User;
import personal.yeongyulgori.user.domain.repository.UserRepository;
import personal.yeongyulgori.user.dto.UserResponseDto;
import personal.yeongyulgori.user.service.UserService;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true, timeout = 10)
    public UserResponseDto getUserProfile(String username) {

        log.info("Beginning to retrieve user profile for username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("해당 회원이 존재하지 않습니다. username: " + username));

        log.info("User profile retrieved successfully for username: {}", username);

        return UserResponseDto.of(user.getEmail(), user.getUsername(), user.getName(),
                user.getRole(), user.getCreatedAt(), user.getModifiedAt());

    }

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true, timeout = 30)
    public Page<UserResponseDto> getSearchedUsers(String keyword, Pageable pageable) {

        log.info("Beginning to retrieve searched users by keyword: {}", keyword);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Page<User> users = userRepository.findByNameContaining(keyword, pageable);

        List<UserResponseDto> userResponseDtos = users.getContent().stream()
                .map(user -> UserResponseDto.of(user.getEmail(), user.getUsername(), user.getName(),
                        user.getRole(), user.getCreatedAt(), user.getModifiedAt()))
                .collect(Collectors.toList());

        stopWatch.stop();

        log.info("Searched users retrieved successfully: {}\n Retrieving task execution time: {} ms",
                keyword, stopWatch.getTotalTimeMillis());

        return new PageImpl<>(userResponseDtos, pageable, users.getTotalElements());

    }

}
