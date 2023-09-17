package personal.yeongyulgori.user.controller;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import personal.yeongyulgori.user.model.dto.UserResponseDto;
import personal.yeongyulgori.user.service.AutoCompleteService;
import personal.yeongyulgori.user.service.UserService;

import java.util.List;

@RestController
@RequestMapping("users/v1")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AutoCompleteService autoCompleteService;

    @ApiOperation(value = "회원 프로필 조회", notes = "다른 회원의 프로필을 조회할 수 있습니다.")
    @GetMapping("{username}")
    public ResponseEntity<UserResponseDto> getUserProfile(
            @PathVariable @ApiParam(value = "사용자 이름", example = "gildong1234") String username
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserProfile(username));
    }

    @ApiOperation(value = "회원 목록", notes = "일부 성명 키워드를 입력해 해당하는 회원 목록을 조회할 수 있습니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "page", value = "현재 페이지 번호", dataType = "int",
                    paramType = "query", defaultValue = "0"
            ),
            @ApiImplicitParam(
                    name = "size", value = "페이지 당 항목 수", dataType = "int",
                    paramType = "query", defaultValue = "20"
            ),
            @ApiImplicitParam(
                    name = "sort", value = "정렬 방식 (name,asc, name,desc)",
                    dataType = "string", paramType = "query"
            )
    })
    @GetMapping()
    public ResponseEntity<Page<UserResponseDto>> searchUsers(
            @ApiParam(value = "성명 키워드", example = "길동") String keyword, Pageable pageable
    ) {

        Page<UserResponseDto> users = userService.getSearchedUsers(keyword, pageable);

        return ResponseEntity.status(HttpStatus.OK).body(users);

    }

    @ApiOperation(
            value = "키워드로 다른 회원의 성명 검색",
            notes = "성명의 앞부분 키워드를 입력해 자동 완성 결과를 조회할 수 있습니다."
    )
    @GetMapping("/auto-complete")
    public ResponseEntity<?> autoComplete(@RequestParam @ApiParam(value = "키워드", example = "홍길") String keyword) {

        List<String> autoCompleteResults = autoCompleteService.autoComplete(keyword);

        return ResponseEntity.status(HttpStatus.OK).body(autoCompleteResults);

    }

}
