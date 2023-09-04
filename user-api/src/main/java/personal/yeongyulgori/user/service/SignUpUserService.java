package personal.yeongyulgori.user.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import personal.yeongyulgori.user.domain.SignUpForm;
import personal.yeongyulgori.user.domain.model.User;
import personal.yeongyulgori.user.domain.repository.UserRepository;
import personal.yeongyulgori.user.dto.UserResponseDto;

@Service
@RequiredArgsConstructor
public class SignUpUserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public UserResponseDto signUpUser(SignUpForm signUpForm) {
        return modelMapper.map(userRepository.save(User.from(signUpForm)), UserResponseDto.class);
    }

}
