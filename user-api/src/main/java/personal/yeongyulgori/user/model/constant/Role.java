package personal.yeongyulgori.user.model.constant;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Role {

    ROLE_ADMIN("관리자"),
    ROLE_GENERAL_USER("일반 사용자"),
    ROLE_BUSINESS_USER("기업 고객");

    private final String description;

}
