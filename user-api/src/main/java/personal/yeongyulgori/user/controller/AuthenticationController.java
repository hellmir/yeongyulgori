package personal.yeongyulgori.user.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import personal.yeongyulgori.user.model.dto.CrucialInformationUpdateDto;
import personal.yeongyulgori.user.model.dto.SignInResponseDto;
import personal.yeongyulgori.user.model.dto.UserResponseDto;
import personal.yeongyulgori.user.model.form.InformationUpdateForm;
import personal.yeongyulgori.user.model.form.SignInForm;
import personal.yeongyulgori.user.model.form.SignUpForm;
import personal.yeongyulgori.user.security.JwtTokenProvider;
import personal.yeongyulgori.user.service.AuthenticationService;
import personal.yeongyulgori.user.validation.group.OnSignIn;
import personal.yeongyulgori.user.validation.group.OnSignUp;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("users/v1")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final JwtTokenProvider jwtTokenProvider;

    @ApiOperation(value = "회원 가입", notes = "회원 가입 양식을 입력해 회원 가입을 할 수 있습니다.")
    @PostMapping("signup")
    public ResponseEntity<UserResponseDto> signUpUser
            (@Validated(OnSignUp.class)
             @RequestBody @ApiParam(value = "회원 가입 양식")
             SignUpForm signUpForm) {

        UserResponseDto userResponseDto = authenticationService.signUpUser(signUpForm);

        return buildResponse(userResponseDto);

    }

    @ApiOperation(value = "로그인", notes = "email 또는 username과 비밀번호를 입력해 로그인을 할 수 있습니다.")
    @PostMapping("login")
    public ResponseEntity<String> signInUser
            (@Validated(OnSignIn.class)
             @RequestBody @ApiParam(value = "로그인 양식")
             SignInForm signInForm) {

        SignInResponseDto signInResponseDto = authenticationService.signInUser(signInForm);

        String token = jwtTokenProvider.generateToken(signInResponseDto.getUsername(), signInResponseDto.getRoles());

        return ResponseEntity.status(HttpStatus.OK).body(token);

    }

    @ApiOperation(value = "회원 개인정보 조회", notes = "사용자 이름을 입력해 회원 개인정보를 조회할 수 있습니다.")
    @GetMapping("{username}/details")
    public ResponseEntity<UserResponseDto> getUserInformation
            (@PathVariable @ApiParam(value = "사용자 이름", example = "gildong1234") String username) {

        UserResponseDto userResponseDto = authenticationService.getUserDetails(username);

        return ResponseEntity.status(HttpStatus.OK).body(userResponseDto);

    }

    @ApiOperation(value = "회원 정보 수정",
            notes = "수정할 변수(id 제외)를 1개 또는 여러 개 입력해 회원 정보를 수정할 수 있습니다.")
    @PatchMapping("{username}")
    public ResponseEntity<UserResponseDto> updateUserInformation
            (@PathVariable @ApiParam(value = "사용자 이름", example = "gildong1234")
             String username,
             @Valid @RequestBody @ApiParam(value = "회원 정보 수정 양식")
             InformationUpdateForm informationUpdateForm) {

        UserResponseDto userResponseDto = authenticationService
                .updateUserInformation(username, informationUpdateForm);

        return username.equals(userResponseDto.getUsername()) ?
                ResponseEntity.status(HttpStatus.OK).body(userResponseDto) : buildResponse(userResponseDto);

    }

    @ApiOperation(value = "주요 회원 정보 수정", notes = "비밀번호를 입력해 하나의 중요한 회원 정보를 수정할 수 있습니다.")
    @PatchMapping("{username}/auth")
    public ResponseEntity<Void> updateUserInformationWithAuthentication
            (@PathVariable @ApiParam(value = "사용자 이름", example = "gildong1234")
             String username,
             @Valid @RequestBody @ApiParam(value = "회원 정보 수정 양식")
             CrucialInformationUpdateDto crucialInformationUpdateDto) {

        authenticationService.updateCrucialUserInformation(username, crucialInformationUpdateDto);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }


    @ApiOperation(value = "비밀번호 재설정 요청", notes = "비밀번호를 잊어 버린 경우 인증을 통해 재설정을 요청할 수 있습니다.")
    @PostMapping("password-reset/request")
    public ResponseEntity<Void> requestPasswordReset
            (@ApiParam(value = "이메일 주소", example = "abcd@abc.com") String email) {

        authenticationService.requestPasswordReset(email);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

    @ApiOperation(value = "비밀번호 재설정", notes = "인증에 성공하면 새로운 비밀번호를 설정할 수 있습니다.")
    @PatchMapping("password-reset")
    public ResponseEntity<Void> resetPassword
            (@ApiParam(value = "비밀번호", example = "1234") String password) {

        authenticationService.resetPassword(password);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

    @ApiOperation(value = "회원 탈퇴", notes = "비밀번호를 입력해 회원을 탈퇴할 수 있습니다.")
    @DeleteMapping("{username}")
    public ResponseEntity<Void> deleteUser
            (@ApiParam(value = "사용자 이름", example = "gildong1234") @PathVariable String username,
             @ApiParam(value = "비밀번호", example = "1234") String password) {

        authenticationService.deleteUser(username, password);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

    private ResponseEntity<UserResponseDto> buildResponse(UserResponseDto userResponseDto) {

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{username}")
                .buildAndExpand(userResponseDto.getUsername())
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(userResponseDto);

    }

}
