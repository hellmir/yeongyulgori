package personal.yeongyulgori.post.domain.image.model.constant;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TargetType {

    POST("게시물"),
    COMMENT("댓글"),
    REPLY("답글");

    private final String description;

}
