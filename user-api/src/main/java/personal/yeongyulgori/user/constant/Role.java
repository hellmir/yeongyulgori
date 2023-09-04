package personal.yeongyulgori.user.constant;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Role {

    ADMINISTRATOR("관리자"),
    GENERAL_USER("사용자");

    private final String description;

}
