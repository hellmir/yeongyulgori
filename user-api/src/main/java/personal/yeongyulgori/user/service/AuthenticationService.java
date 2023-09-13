package personal.yeongyulgori.user.service;

import personal.yeongyulgori.user.domain.CrucialInformationUpdateForm;
import personal.yeongyulgori.user.domain.InformationUpdateForm;
import personal.yeongyulgori.user.domain.SignInForm;
import personal.yeongyulgori.user.domain.SignUpForm;
import personal.yeongyulgori.user.dto.UserResponseDto;

public interface AuthenticationService {

    UserResponseDto signUpUser(SignUpForm signUpForm);

    String signInUser(SignInForm signInForm);

    UserResponseDto getUserDetails(String username);

    UserResponseDto updateUserInformations(String username, InformationUpdateForm informationUpdateForm);

    void updateCrucialUserInformation(String username, CrucialInformationUpdateForm crucialInformationUpdateForm);

    void requestPasswordReset(String email);

    void resetPassword(String token, String password);

    void deleteUser(String username, String password);

}
