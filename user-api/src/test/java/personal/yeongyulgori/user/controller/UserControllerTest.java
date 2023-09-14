package personal.yeongyulgori.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import personal.yeongyulgori.user.domain.model.User;
import personal.yeongyulgori.user.domain.repository.UserRepository;
import personal.yeongyulgori.user.dto.UserResponseDto;
import personal.yeongyulgori.user.service.AutoCompleteService;
import personal.yeongyulgori.user.service.UserService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static personal.yeongyulgori.user.constant.Role.BUSINESS_USER;
import static personal.yeongyulgori.user.constant.Role.GENERAL_USER;
import static personal.yeongyulgori.user.testutil.TestObjectFactory.createUserWithAddress;

@ActiveProfiles("test")
@WebMvcTest(controllers = UserController.class)
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
    void getUserProfile() throws Exception {

        // given
        String username = "gildong1234";

        User user = createUserWithAddress(
                "abcd@abc.com", username, "1234", "홍길동",
                LocalDate.of(2000, 1, 1), "01012345678", GENERAL_USER
        );

        userRepository.save(user);

        UserResponseDto userResponseDto = UserResponseDto.from(user);

        when(userService.getUserProfile(username)).thenReturn(userResponseDto);

        // when, then
        mockMvc.perform(
                        get("/users/v1/{username}", username)
                )
                .andDo(print())
                .andExpect(status().isOk());

    }

    @DisplayName("일부 성명 키워드를 입력하면 해당하는 회원들의 목록을 조회할 수 있다.")
    @Test
    void searchUsers() throws Exception {

        // given
        String keyword = "길동";

        User user1 = createUserWithAddress(
                "abcd@abc.com", "gildong1234", "1234", "홍길동",
                LocalDate.of(2000, 1, 1), "01012345678", GENERAL_USER
        );

        User user2 = createUserWithAddress(
                "abcd@abcd.com", "gildong12345", "12345", "홍길숙",
                LocalDate.of(2000, 2, 2), "01012345678", BUSINESS_USER
        );

        User user3 = createUserWithAddress(
                "abcd@abcde.com", "gildong123456", "123456", "고길동",
                LocalDate.of(2000, 3, 3), "01012345678", GENERAL_USER
        );

        userRepository.saveAll(List.of(user1, user2, user3));

        Pageable pageable = Pageable.ofSize(10);

        List<UserResponseDto> userResponseDtoList = new ArrayList<>();

        Page<UserResponseDto> userPage = new PageImpl<>(userResponseDtoList, pageable, userResponseDtoList.size());

        when(userService.getSearchedUsers(keyword, pageable)).thenReturn(userPage);

        // when, then
        mockMvc.perform(
                        get("/users/v1")
                )
                .andDo(print())
                .andExpect(status().isOk());

    }

    @DisplayName("일부 성명 키워드를 입력하면 해당하는 회원들의 성명 목록을 조회할 수 있다.")
    @Test
    void autoComplete() throws Exception {

        // given
        String keyword = "홍길";

        User user1 = createUserWithAddress(
                "abcd@abc.com", "gildong1234", "1234", "홍길동",
                LocalDate.of(2000, 1, 1), "01012345678", GENERAL_USER
        );

        User user2 = createUserWithAddress(
                "abcd@abcd.com", "gildong12345", "12345", "고길동",
                LocalDate.of(2000, 2, 2), "01012345678", BUSINESS_USER
        );

        User user3 = createUserWithAddress(
                "abcd@abcde.com", "gildong123456", "123456", "홍길숙",
                LocalDate.of(2000, 3, 3), "01012345678", GENERAL_USER
        );

        userRepository.saveAll(List.of(user1, user2, user3));

        List<String> autoCompleteResults = new ArrayList<>();

        when(autoCompleteService.autoComplete(keyword)).thenReturn(autoCompleteResults);

        // when, then
        mockMvc.perform(
                        get("/users/v1/auto-complete")
                                .queryParam("keyword", keyword)
                )
                .andDo(print())
                .andExpect(status().isOk());

    }

}