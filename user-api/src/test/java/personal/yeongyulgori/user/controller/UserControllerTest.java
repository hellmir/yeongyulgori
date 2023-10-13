package personal.yeongyulgori.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import personal.yeongyulgori.user.model.dto.UserResponseDto;
import personal.yeongyulgori.user.model.entity.User;
import personal.yeongyulgori.user.model.repository.UserRepository;
import personal.yeongyulgori.user.security.CustomAuthenticationEntryPoint;
import personal.yeongyulgori.user.security.JwtAuthenticationFilter;
import personal.yeongyulgori.user.security.JwtTokenProvider;
import personal.yeongyulgori.user.service.AutoCompleteService;
import personal.yeongyulgori.user.service.UserService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static personal.yeongyulgori.user.model.constant.Role.*;
import static personal.yeongyulgori.user.testutil.TestConstant.*;
import static personal.yeongyulgori.user.testutil.TestObjectFactory.createUserWithAddress;

@ActiveProfiles("test")
@WebMvcTest(controllers = UserController.class)
@Import({JwtTokenProvider.class, JwtAuthenticationFilter.class, CustomAuthenticationEntryPoint.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private AutoCompleteService autoCompleteService;

    @MockBean
    private UserRepository userRepository;

    @DisplayName("사용자 이름을 입력하면 다른 회원의 프로필을 조회할 수 있다.")
    @Test
    @WithMockUser
    void getUserProfile() throws Exception {

        // given
        User user = createUserWithAddress(EMAIL1, USERNAME1, PASSWORD1, FULL_NAME1,
                BIRTH_DATE1, PHONE_NUMBER1, List.of(ROLE_GENERAL_USER));

        userRepository.save(user);

        UserResponseDto userResponseDto = UserResponseDto.from(user);

        when(userService.getUserProfile(USERNAME1)).thenReturn(userResponseDto);

        // when, then
        mockMvc.perform(get("/users/v1/{username}", USERNAME1))
                .andDo(print())
                .andExpect(status().isOk());

    }

    @DisplayName("일부 성명 키워드를 입력하면 해당하는 회원들의 목록을 조회할 수 있다.")
    @Test
    @WithMockUser
    void searchUsers() throws Exception {

        // given
        String keyword = FIRST_NAME;

        User user1 = createUserWithAddress(
                EMAIL1, USERNAME1, PASSWORD1, FULL_NAME1,
                LocalDate.of(2000, 1, 1), PHONE_NUMBER1, List.of(ROLE_GENERAL_USER)
        );

        User user2 = createUserWithAddress(
                EMAIL2, USERNAME2, PASSWORD2, FULL_NAME5,
                LocalDate.of(2000, 2, 2), PHONE_NUMBER2, List.of(ROLE_BUSINESS_USER)
        );

        User user3 = createUserWithAddress(
                EMAIL3, USERNAME3, PASSWORD3, FULL_NAME2,
                LocalDate.of(2000, 3, 3), PHONE_NUMBER3, List.of(ROLE_ADMIN)
        );

        userRepository.saveAll(List.of(user1, user2, user3));

        Pageable pageable = Pageable.ofSize(10);

        List<UserResponseDto> userResponseDtoList = new ArrayList<>();

        Page<UserResponseDto> userPage = new PageImpl<>(userResponseDtoList, pageable, userResponseDtoList.size());

        when(userService.getSearchedUsers(keyword, pageable)).thenReturn(userPage);

        // when, then
        mockMvc.perform(get("/users/v1")
                        .queryParam("keyword", keyword))
                .andDo(print())
                .andExpect(status().isOk());

    }

    @DisplayName("성명 키워드를 입력하지 않으면 전체 회원 목록을 조회할 수 있다.")
    @Test
    @WithMockUser
    void searchUsersWithoutKeyword() throws Exception {

        // given
        String keyword = "";

        User user1 = createUserWithAddress(
                EMAIL1, USERNAME1, PASSWORD1, FULL_NAME1,
                LocalDate.of(2000, 1, 1), PHONE_NUMBER1, List.of(ROLE_GENERAL_USER)
        );

        User user2 = createUserWithAddress(
                EMAIL2, USERNAME2, PASSWORD2, FULL_NAME5,
                LocalDate.of(2000, 2, 2), PHONE_NUMBER2, List.of(ROLE_BUSINESS_USER)
        );

        User user3 = createUserWithAddress(
                EMAIL3, USERNAME3, PASSWORD3, FULL_NAME2,
                LocalDate.of(2000, 3, 3), PHONE_NUMBER3, List.of(ROLE_ADMIN)
        );

        userRepository.saveAll(List.of(user1, user2, user3));

        Pageable pageable = Pageable.ofSize(10);

        List<UserResponseDto> userResponseDtoList = new ArrayList<>();

        Page<UserResponseDto> userPage = new PageImpl<>(userResponseDtoList, pageable, userResponseDtoList.size());

        when(userService.getSearchedUsers(keyword, pageable)).thenReturn(userPage);

        // when, then
        mockMvc.perform(get("/users/v1"))
                .andDo(print())
                .andExpect(status().isOk());

    }

    @DisplayName("일부 성명 키워드를 입력하면 해당하는 회원들의 성명 자동완성 목록을 조회할 수 있다.")
    @Test
    @WithMockUser
    void autoComplete() throws Exception {

        // given
        String keyword = "홍길";

        User user1 = createUserWithAddress(EMAIL1, USERNAME1, PASSWORD1, FULL_NAME1,
                LocalDate.of(2000, 1, 1), PHONE_NUMBER1, List.of(ROLE_GENERAL_USER));

        User user2 = createUserWithAddress(EMAIL2, USERNAME2, PASSWORD2, FULL_NAME2,
                LocalDate.of(2000, 2, 2), PHONE_NUMBER2, List.of(ROLE_BUSINESS_USER));

        User user3 = createUserWithAddress(EMAIL3, USERNAME3, PASSWORD3, FULL_NAME5,
                LocalDate.of(2000, 3, 3), PHONE_NUMBER3, List.of(ROLE_ADMIN));

        userRepository.saveAll(List.of(user1, user2, user3));

        List<String> autoCompleteResults = new ArrayList<>();

        when(autoCompleteService.autoComplete(keyword)).thenReturn(autoCompleteResults);

        // when, then
        mockMvc.perform(get("/users/v1/auto-complete"))
                .andDo(print())
                .andExpect(status().isOk());

    }

}
