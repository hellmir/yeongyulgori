package personal.yeongyulgori.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import personal.yeongyulgori.user.model.constant.Role;
import personal.yeongyulgori.user.model.dto.CrucialInformationUpdateDto;
import personal.yeongyulgori.user.model.dto.PasswordRequestDto;
import personal.yeongyulgori.user.model.dto.SignInResponseDto;
import personal.yeongyulgori.user.model.dto.UserResponseDto;
import personal.yeongyulgori.user.model.entity.User;
import personal.yeongyulgori.user.model.entity.embedment.Address;
import personal.yeongyulgori.user.model.form.InformationUpdateForm;
import personal.yeongyulgori.user.model.form.SignInForm;
import personal.yeongyulgori.user.model.form.SignUpForm;
import personal.yeongyulgori.user.model.repository.UserRepository;
import personal.yeongyulgori.user.security.CustomAuthenticationEntryPoint;
import personal.yeongyulgori.user.security.JwtAuthenticationFilter;
import personal.yeongyulgori.user.security.JwtTokenProvider;
import personal.yeongyulgori.user.service.AuthenticationService;
import personal.yeongyulgori.user.service.AutoCompleteService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static personal.yeongyulgori.user.model.constant.Role.ROLE_BUSINESS_USER;
import static personal.yeongyulgori.user.model.constant.Role.ROLE_GENERAL_USER;
import static personal.yeongyulgori.user.testutil.TestConstant.*;
import static personal.yeongyulgori.user.testutil.TestObjectFactory.*;

@ActiveProfiles("test")
@WebMvcTest(controllers = AuthenticationController.class)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthenticationController authenticationController;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private AutoCompleteService autoCompleteService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @AfterEach
    void tearDown() {
        userRepository.deleteAllInBatch();
    }

    @DisplayName("사용자가 올바른 회원 가입 양식을 입력하면 회원 가입을 할 수 있다.")
    @Test
    @WithMockUser
    void signUpUser() throws Exception {

        // given
        SignUpForm signUpForm = enterUserFormWithAddress
                (EMAIL1, USERNAME1, PASSWORD1, FULL_NAME1,
                        BIRTH_DATE1, PHONE_NUMBER1, List.of(ROLE_GENERAL_USER));

        UserResponseDto userResponseDto = UserResponseDto.of(
                EMAIL1, USERNAME1, FULL_NAME1,
                List.of(ROLE_GENERAL_USER), LocalDateTime.now(), LocalDateTime.now()
        );

        when(authenticationService.signUpUser(any(SignUpForm.class))).thenReturn(userResponseDto);

        // when, then
        mockMvc.perform(post("/users/v1/signup")
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(signUpForm))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated());

    }

    @DisplayName("올바른 로그인 양식을 입력하면 로그인을 할 수 있다.")
    @Test
    @WithMockUser
    void signInUser() throws Exception {

        // given
        SignUpForm signUpForm = enterUserFormWithAddress
                (EMAIL1, USERNAME1, PASSWORD1, FULL_NAME1, BIRTH_DATE1,
                        PHONE_NUMBER1, List.of(Role.ROLE_GENERAL_USER));

        when(authenticationService.signInUser(any(SignInForm.class)))
                .thenReturn(SignInResponseDto.of(USERNAME1, List.of(Role.ROLE_GENERAL_USER)));
        ;

        // when, then
        mockMvc.perform(post("/users/v1/login")
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(signUpForm))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

    }

    @DisplayName("사용자 이름을 입력하면 회원 개인정보를 조회할 수 있다.")
    @Test
    @WithMockUser
    void getUserInformation() throws Exception {

        // given
        String username = USERNAME1;

        User user = createUserWithAddress(EMAIL1, username, PASSWORD1,
                FULL_NAME1, BIRTH_DATE1,
                PHONE_NUMBER1, List.of(ROLE_GENERAL_USER));

        userRepository.save(user);

        UserResponseDto userResponseDto = UserResponseDto.from(user);

        when(authenticationService.getUserDetails(username)).thenReturn(userResponseDto);

        // when, then
        mockMvc.perform(get("/users/v1/{username}/details", username))
                .andDo(print())
                .andExpect(status().isOk());

    }

    @DisplayName("하나의 수정할 변수를 입력해 회원 정보를 수정할 수 있다.")
    @Test
    @WithMockUser
    void updateUserInformationWithOneVariable() throws Exception {

        // given
        String username = USERNAME1;

        User user = createUser(EMAIL1, username, PASSWORD1, FULL_NAME1,
                BIRTH_DATE1, PHONE_NUMBER1, List.of(ROLE_GENERAL_USER));

        userRepository.save(user);

        InformationUpdateForm informationUpdateForm = InformationUpdateForm.builder()
                .id(1L)
                .fullName(FULL_NAME2)
                .build();

        UserResponseDto userResponseDto = UserResponseDto.from(user);

        when(authenticationService
                .updateUserInformation(eq(username), any(InformationUpdateForm.class)))
                .thenReturn(userResponseDto);

        // when, then
        mockMvc.perform(patch("/users/v1/{username}", username)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(informationUpdateForm))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

    }

    @DisplayName("여러 개의 수정할 변수를 입력해 회원 정보를 수정할 수 있다.")
    @Test
    @WithMockUser
    void updateUserInformationWithVariables() throws Exception {

        // given
        String username = USERNAME1;

        User user = createUser(EMAIL1, username, PASSWORD1, FULL_NAME1,
                BIRTH_DATE1, PHONE_NUMBER1, List.of(ROLE_GENERAL_USER));

        userRepository.save(user);

        InformationUpdateForm informationUpdateForm = InformationUpdateForm.builder()
                .id(1L)
                .fullName(FULL_NAME2)
                .roles(List.of(ROLE_BUSINESS_USER))
                .address(Address.builder()
                        .city(CITY)
                        .build())
                .build();

        UserResponseDto userResponseDto = UserResponseDto.from(user);

        when(authenticationService
                .updateUserInformation(eq(username), any(InformationUpdateForm.class)))
                .thenReturn(userResponseDto);

        // when, then
        mockMvc.perform(patch("/users/v1/{username}", username)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(informationUpdateForm))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

    }

    @DisplayName("하나의 수정할 변수를 입력해 중요한 회원 정보를 수정할 수 있다.")
    @Test
    @WithMockUser
    void updateUserInformationWithAuthentication() throws Exception {

        // given
        String username = USERNAME1;

        User user = createUser(EMAIL1, username, PASSWORD1, FULL_NAME1,
                BIRTH_DATE1, PHONE_NUMBER1, List.of(ROLE_GENERAL_USER));

        userRepository.save(user);

        CrucialInformationUpdateDto crucialInformationUpdateDto = CrucialInformationUpdateDto.builder()
                .id(1L)
                .email(EMAIL2)
                .build();

        // when, then
        mockMvc.perform(patch("/users/v1/{username}/auth", username)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(crucialInformationUpdateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());

    }

    @DisplayName("이메일 인증을 통해 비밀번호 재설정을 요청할 수 있다.")
    @Test
    @WithMockUser
    void requestPasswordReset() throws Exception {

        // given
        User user = createUser(EMAIL1, USERNAME1, PASSWORD1, FULL_NAME1,
                BIRTH_DATE1, PHONE_NUMBER1, List.of(ROLE_GENERAL_USER));

        userRepository.save(user);

        // when, then
        mockMvc.perform(post("/users/v1/password-reset/request")
                        .with(csrf())
                        .param("email", user.getEmail())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

    }

    @DisplayName("이메일 인증에 성공하면 토큰을 통해 비밀번호를 재설정할 수 있다.")
    @Test
    @WithMockUser
    void resetPassword() throws Exception {

        // given
        User user = createUser(EMAIL1, USERNAME1, PASSWORD1, FULL_NAME1,
                BIRTH_DATE1, PHONE_NUMBER1, List.of(ROLE_GENERAL_USER));

        userRepository.save(user);

        PasswordRequestDto passwordRequestDto = new PasswordRequestDto(user.getPassword());

        String token = "abcdefghijklmnopqrstuvwxyz";

        // when, then
        mockMvc.perform(patch("/users/v1/password-reset")
                        .with(csrf())
                        .param("token", token)
                        .content(objectMapper.writeValueAsString(passwordRequestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());

    }

    @DisplayName("비밀번호 인증을 통해 회원을 탈퇴할 수 있다.")
    @Test
    @WithMockUser
    void deleteUser() throws Exception {

        // given
        User user = createUser(EMAIL1, USERNAME1, PASSWORD1, FULL_NAME1,
                BIRTH_DATE1, PHONE_NUMBER1, List.of(ROLE_GENERAL_USER));

        userRepository.save(user);

        PasswordRequestDto passwordRequestDto = new PasswordRequestDto(user.getPassword());

        // when, then
        mockMvc.perform(delete("/users/v1/{username}", user.getUsername())
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(passwordRequestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());

    }

}
