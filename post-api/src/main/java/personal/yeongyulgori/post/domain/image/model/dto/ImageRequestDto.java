package personal.yeongyulgori.post.domain.image.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import personal.yeongyulgori.post.domain.image.model.constant.TargetType;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageRequestDto {

    @ApiModelProperty(value = "이미지를 포함하는 대상 유형: POST, COMMENT, REPLY", example = "POST")
    private TargetType targetType;

    @ApiModelProperty(value = "이미지 이름", example = "test_image")
    private String name;

    @ApiModelProperty
            (value = "이미지 URL",
                    example = "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample1.jpg")
    private String url;

    @ApiModelProperty(value = "게시물, 댓글, 답글 내용에서 이미지의 위치", example = "10")
    private int positionInContent;

}
