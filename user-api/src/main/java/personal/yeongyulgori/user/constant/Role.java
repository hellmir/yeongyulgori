package personal.yeongyulgori.user.constant;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Role {

    GENERAL_USER("일반 사용자"),
    BUSINESS_USER("기업 고객");

    private final String description;

}
