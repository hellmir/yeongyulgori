package personal.yeongyulgori.post.domain.post.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostRequestDto {

    @ApiModelProperty(value = "게시물 내용", example = "안녕하세요?")
    @NotNull
    private String content;

}
