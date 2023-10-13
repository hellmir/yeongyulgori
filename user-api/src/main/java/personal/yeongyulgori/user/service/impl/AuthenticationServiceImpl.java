package personal.yeongyulgori.user.service.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import personal.yeongyulgori.user.exception.general.sub.DuplicateUserException;
import personal.yeongyulgori.user.exception.general.sub.DuplicateUsernameException;
import personal.yeongyulgori.user.exception.serious.sub.NonExistentUserException;
import personal.yeongyulgori.user.exception.significant.sub.IncorrectPasswordException;
import personal.yeongyulgori.user.exception.significant.sub.TokenExpiredException;
import personal.yeongyulgori.user.model.dto.CrucialInformationUpdateDto;
import personal.yeongyulgori.user.model.dto.PasswordRequestDto;
import personal.yeongyulgori.user.model.dto.SignInResponseDto;
import personal.yeongyulgori.user.model.dto.UserResponseDto;
import personal.yeongyulgori.user.model.entity.PasswordResetToken;
import personal.yeongyulgori.user.model.entity.User;
import personal.yeongyulgori.user.model.form.InformationUpdateForm;
import personal.yeongyulgori.user.model.form.SignInForm;
import personal.yeongyulgori.user.model.form.SignUpForm;
import personal.yeongyulgori.user.model.repository.PasswordResetTokenRepository;
import personal.yeongyulgori.user.model.repository.UserRepository;
import personal.yeongyulgori.user.service.AuthenticationService;
import personal.yeongyulgori.user.service.AutoCompleteService;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Isolation.REPEATABLE_READ;

/**
 * 회원 서비스
 */
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService, UserDetailsService {

    private final AutoCompleteService autoCompleteService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Value("${spring.redis.host}")
    private String ec2Ip;

    @Value(("${server.port}"))
    private String serverPort;

    @Override
    public UserDetails loadUserByUsername(String emailOrUsername) throws UsernameNotFoundException {
        return validateUserExists(emailOrUsername);
    }

    @Override
    @Transactional(isolation = REPEATABLE_READ, timeout = 20)
    public UserResponseDto signUpUser(SignUpForm signUpForm) {

        validateNotDuplicateUser(signUpForm.getUsername(), signUpForm.getEmail(), signUpForm.getPhoneNumber());

        User savedUser = userRepository.save(User.from(signUpForm, passwordEncoder.encode(signUpForm.getPassword())));

        autoCompleteService.addAutoCompleteKeyWord(savedUser.getFullName());

        return UserResponseDto.of(savedUser.getEmail(), savedUser.getUsername(), savedUser.getFullName(),
                savedUser.getRoles(), savedUser.getCreatedAt(), savedUser.getModifiedAt());

    }

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true, timeout = 10)
    public SignInResponseDto signInUser(SignInForm signInForm) {

        User signedUpUser = validateUserExists(signInForm.getEmailOrUsername());

        validatePasswordIsCorrect(signInForm.getPassword(), signedUpUser.getPassword());

        return SignInResponseDto.of(signedUpUser.getUsername(), signedUpUser.getRoles());

    }

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true, timeout = 10)
    public UserResponseDto getUserDetails(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NonExistentUserException("해당 회원이 존재하지 않습니다. username: " + username));

        return UserResponseDto.from(user);

    }

    @Override
    public UserResponseDto updateUserInformation(String username, InformationUpdateForm informationUpdateForm) {

        User user = userRepository.findById(informationUpdateForm.getId())
                .orElseThrow(() -> new NonExistentUserException
                        ("해당 회원이 존재하지 않습니다. username: " + username));

        User updatedUser = userRepository.save(user.withForm(username, informationUpdateForm));

        return UserResponseDto.from(updatedUser);

    }

    @Override
    public void updateCrucialUserInformation
            (String username, CrucialInformationUpdateDto crucialInformationUpdateDto) {

        User user = userRepository.findById(crucialInformationUpdateDto.getId())
                .orElseThrow(() -> new NonExistentUserException("해당 회원이 존재하지 않습니다. username: " + username));

        userRepository.save(user.withCrucialData(crucialInformationUpdateDto));

    }

    // TODO(추후 mailgun 서비스 이용)
    @Override
    @Transactional(isolation = READ_COMMITTED, timeout = 10)
    public String requestPasswordReset(String email, String token) {

        User user = validateEmailIsValid(email);

        PasswordResetToken passwordResetToken = PasswordResetToken.of(token, user);

        passwordResetTokenRepository.save(passwordResetToken);

        String passwordResetUrl = "http://" + ec2Ip + ":" + serverPort + "/password-reset?token=" + token;

        return passwordResetUrl;

    }

    @Override
    @Transactional(isolation = READ_COMMITTED, timeout = 10)
    public void resetPassword(String token, PasswordRequestDto passwordRequestDto) {

        PasswordResetToken passwordResetToken = validateTokenIsValid(token);

        User user = passwordResetToken.getUser();
        User changedUser = user.withPassword(passwordRequestDto);
        userRepository.save(changedUser);

        passwordResetTokenRepository.delete(passwordResetToken);

    }

    @Override
    @Transactional(isolation = READ_COMMITTED, timeout = 10)
    public void deleteUser(String username, PasswordRequestDto passwordRequestDto) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NonExistentUserException("해당 회원이 존재하지 않습니다. username: " + username));

        validatePasswordIsCorrect(passwordRequestDto.getPassword(), user.getPassword());

        userRepository.delete(user);

        boolean isFullNameRemained = userRepository.existsByFullName(user.getFullName());

        if (!isFullNameRemained) {
            autoCompleteService.deleteAutoCompleteKeyword(user.getFullName());
        }

    }

    private void validateNotDuplicateUser(String username, String email, String phoneNumber) {

        if (userRepository.existsByUsername(username)) {
            throw new DuplicateUsernameException("중복된 사용자 이름입니다. username: " + username);
        }

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateUserException("이미 가입된 이메일입니다. email: " + email);
        }

        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new DuplicateUserException("이미 가입된 전화번호입니다. phoneNumber: " + phoneNumber);
        }

    }

    private void validatePasswordIsCorrect(String password1, String password2) {

        if (!passwordEncoder.matches(password1, password2)) {
            throw new IncorrectPasswordException();
        }

    }

    private User validateUserExists(String emailOrUsername) {

        return emailOrUsername.contains("@")
                ? userRepository.findByEmail(emailOrUsername)
                .orElseThrow(() -> new EntityNotFoundException
                        ("해당 회원이 존재하지 않습니다. email: " + emailOrUsername))
                : userRepository.findByUsername(emailOrUsername)
                .orElseThrow(() -> new EntityNotFoundException
                        ("해당 회원이 존재하지 않습니다. username: " + emailOrUsername));

    }

    private User validateEmailIsValid(String email) {

        if (!email.contains("@")) {
            throw new IllegalArgumentException("email 형식이 올바르지 않습니다. 예: abcd@abc.com");
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NonExistentUserException("해당 회원이 존재하지 않습니다. email: " + email));
    }

    private PasswordResetToken validateTokenIsValid(String token) {

        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findById(token)
                .orElseThrow(() -> new EntityNotFoundException
                        ("토큰이 유효하지 않습니다. 잘못된 URL이 입력되었을 수 있습니다."));

        if (passwordResetToken.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException();
        }

        return passwordResetToken;

    }

}
