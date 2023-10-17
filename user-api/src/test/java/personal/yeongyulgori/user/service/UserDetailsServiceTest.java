package personal.yeongyulgori.user.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import personal.yeongyulgori.user.model.entity.User;
import personal.yeongyulgori.user.model.form.SignUpForm;
import personal.yeongyulgori.user.model.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static personal.yeongyulgori.user.model.constant.Role.*;
import static personal.yeongyulgori.user.testutil.TestConstant.*;
import static personal.yeongyulgori.user.testutil.TestObjectFactory.enterUserForm;

@ActiveProfiles("test")
@SpringBootTest
public class UserDetailsServiceTest {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserRepository userRepository;

    @DisplayName("가입된 사용자의 인증 정보와 권한 정보를 불러 올 수 있다.")
    @Test
    void loadUserByUsername() {

        // given
        SignUpForm signUpForm1 = enterUserForm
                (EMAIL1, USERNAME1, PASSWORD1, FULL_NAME1, BIRTH_DATE1,
                        PHONE_NUMBER1, List.of(ROLE_GENERAL_USER));

        SignUpForm signUpForm2 = enterUserForm
                (EMAIL2, USERNAME2, PASSWORD2, FULL_NAME2, BIRTH_DATE2,
                        PHONE_NUMBER2, List.of(ROLE_BUSINESS_USER));

        SignUpForm signUpForm3 = enterUserForm
                (EMAIL3, USERNAME3, PASSWORD3, FULL_NAME3, BIRTH_DATE3,
                        PHONE_NUMBER3, List.of(ROLE_ADMIN));

        SignUpForm signUpForm4 = enterUserForm
                (EMAIL4, USERNAME4, PASSWORD3, FULL_NAME4, BIRTH_DATE3,
                        PHONE_NUMBER4, List.of(ROLE_GENERAL_USER, ROLE_BUSINESS_USER));

        authenticationService.signUpUser(signUpForm1);
        authenticationService.signUpUser(signUpForm2);
        authenticationService.signUpUser(signUpForm3);
        authenticationService.signUpUser(signUpForm4);

        // when
        List<User> users = userRepository.findAll();

        UserDetails user1 = userDetailsService.loadUserByUsername(USERNAME1);
        List<GrantedAuthority> authorities1 = new ArrayList<>(user1.getAuthorities());

        UserDetails user2 = userDetailsService.loadUserByUsername(USERNAME2);
        List<GrantedAuthority> authorities2 = new ArrayList<>(user2.getAuthorities());

        UserDetails user3 = userDetailsService.loadUserByUsername(USERNAME3);
        List<GrantedAuthority> authorities3 = new ArrayList<>(user3.getAuthorities());

        UserDetails user4 = userDetailsService.loadUserByUsername(USERNAME4);
        List<GrantedAuthority> authorities4 = new ArrayList<>(user4.getAuthorities());


        // then
        assertThat(user1.getUsername()).isEqualTo(USERNAME1);
        assertThat(user1.getAuthorities()).isEqualTo(users.get(0).getAuthorities());

        assertThat(user2.getUsername()).isEqualTo(USERNAME2);
        assertThat(user2.getAuthorities()).isEqualTo(users.get(1).getAuthorities());

        assertThat(user3.getUsername()).isEqualTo(USERNAME3);
        assertThat(user3.getAuthorities()).isEqualTo(users.get(2).getAuthorities());

        assertThat(user4.getUsername()).isEqualTo(USERNAME4);
        assertThat(user4.getAuthorities()).isEqualTo(users.get(3).getAuthorities());

    }

}
