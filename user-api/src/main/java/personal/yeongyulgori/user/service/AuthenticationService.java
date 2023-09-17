package personal.yeongyulgori.user.service;

import personal.yeongyulgori.user.model.dto.CrucialInformationUpdateDto;
import personal.yeongyulgori.user.model.dto.UserResponseDto;
import personal.yeongyulgori.user.model.form.InformationUpdateForm;
import personal.yeongyulgori.user.model.form.SignInForm;
import personal.yeongyulgori.user.model.form.SignUpForm;

public interface AuthenticationService {

    UserResponseDto signUpUser(SignUpForm signUpForm);

    String signInUser(SignInForm signInForm);

    UserResponseDto getUserDetails(String username);

    UserResponseDto updateUserInformation(String username, InformationUpdateForm informationUpdateForm);

    void updateCrucialUserInformation(String username, CrucialInformationUpdateDto crucialInformationUpdateDto);

    void requestPasswordReset(String email);

    void resetPassword(String token, String password);

    void deleteUser(String username, String password);

}
