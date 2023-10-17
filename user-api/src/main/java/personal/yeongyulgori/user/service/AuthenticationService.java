package personal.yeongyulgori.user.service;

import personal.yeongyulgori.user.model.dto.CrucialInformationUpdateDto;
import personal.yeongyulgori.user.model.dto.PasswordRequestDto;
import personal.yeongyulgori.user.model.dto.SignInResponseDto;
import personal.yeongyulgori.user.model.dto.UserResponseDto;
import personal.yeongyulgori.user.model.form.InformationUpdateForm;
import personal.yeongyulgori.user.model.form.SignInForm;
import personal.yeongyulgori.user.model.form.SignUpForm;

public interface AuthenticationService {

    UserResponseDto signUpUser(SignUpForm signUpForm);

    SignInResponseDto signInUser(SignInForm signInForm);

    UserResponseDto getUserDetails(String username);

    UserResponseDto updateUserInformation(String username, InformationUpdateForm informationUpdateForm);

    void updateCrucialUserInformation(String username, CrucialInformationUpdateDto crucialInformationUpdateDto);

    String requestPasswordReset(String email, String token);

    void resetPassword(String token, PasswordRequestDto passwordRequestDto);

    void deleteUser(String username, PasswordRequestDto passwordRequestDto);

}
