package personal.yeongyulgori.user.service.impl;


import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import personal.yeongyulgori.user.domain.InformationUpdateForm;
import personal.yeongyulgori.user.domain.SignInForm;
import personal.yeongyulgori.user.domain.SignUpForm;
import personal.yeongyulgori.user.domain.model.User;
import personal.yeongyulgori.user.domain.repository.UserRepository;
import personal.yeongyulgori.user.dto.CrucialInformationUpdateDto;
import personal.yeongyulgori.user.dto.UserResponseDto;
import personal.yeongyulgori.user.exception.general.sub.DuplicateUserException;
import personal.yeongyulgori.user.exception.general.sub.DuplicateUsernameException;
import personal.yeongyulgori.user.exception.serious.sub.NonExistentUserException;
import personal.yeongyulgori.user.exception.significant.sub.IncorrectPasswordException;
import personal.yeongyulgori.user.service.AuthenticationService;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    @Override
    public UserResponseDto signUpUser(SignUpForm signUpForm) {

        log.info("Beginning to sign up user for email: {}, username: {}",
                signUpForm.getEmail(), signUpForm.getUsername());

        validateDuplicateUser(signUpForm.getEmail(), signUpForm.getUsername(), signUpForm.getPhoneNumber());

        User savedUser = userRepository.save(User.from(signUpForm));

        log.info("User signed up successfully for email: {}, username: {}",
                savedUser.getEmail(), savedUser.getUsername());

        return UserResponseDto.of(
                savedUser.getEmail(), savedUser.getUsername(), savedUser.getName(),
                savedUser.getRole(), savedUser.getCreatedAt(), savedUser.getModifiedAt()
        );

    }

    // TODO
    @Override
    public String signInUser(SignInForm signInForm) {
        return null;
    }

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true, timeout = 10)
    public UserResponseDto getUserDetails(String username) {

        log.info("Beginning to retrieve user profile for username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NonExistentUserException("해당 회원이 존재하지 않습니다. username: " + username));

        log.info("User profile retrieved successfully for username: {}", username);

        return UserResponseDto.from(user);

    }

    @Override
    public UserResponseDto updateUserInformation(String username, InformationUpdateForm informationUpdateForm) {

        log.info("Beginning to update user information for username: {}", username);

        User user = userRepository.findById(informationUpdateForm.getId())
                .orElseThrow(() -> new NonExistentUserException
                        ("해당 회원이 존재하지 않습니다. username: " + username));

        User updatedUser = userRepository.save(user.withForm(username, informationUpdateForm));

        log.info("User information updated successfully for username: {}", updatedUser.getUsername());

        return UserResponseDto.from(updatedUser);

    }

    @Override
    public void updateCrucialUserInformation(
            String username, CrucialInformationUpdateDto crucialInformationUpdateDto
    ) {

        log.info("Beginning to update crucial user information for username: {}", username);

        User user = userRepository.findById(crucialInformationUpdateDto.getId())
                .orElseThrow(() -> new NonExistentUserException("해당 회원이 존재하지 않습니다. username: " + username));

        userRepository.save(user.withCrucialData(crucialInformationUpdateDto));

        log.info("Crucial user information updated successfully for username: {}", username);

    }

    // TODO
    @Override
    public void requestPasswordReset(String email) {

    }

    // TODO
    @Override
    public void resetPassword(String token, String password) {

    }

    // TODO
    @Override
    public void deleteUser(String username, String password) {

        log.info("Beginning to delete user with username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NonExistentUserException("해당 회원이 존재하지 않습니다. username: " + username));

        if (!user.getPassword().equals(password)) {
            throw new IncorrectPasswordException();
        }

        userRepository.delete(user);

        log.info("User deleted successfully for username: {}", username);

    }

    private void validateDuplicateUser(String email, String username, String phoneNumber) {

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateUserException("이미 가입된 회원입니다. email: " + email);
        }

        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new DuplicateUserException("이미 가입된 회원입니다. phoneNumber: " + phoneNumber);
        }

        if (userRepository.existsByUsername(username)) {
            throw new DuplicateUsernameException("중복된 사용자 이름입니다. username: " + username);
        }

    }

}
