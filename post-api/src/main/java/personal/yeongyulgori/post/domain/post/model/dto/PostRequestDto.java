package personal.yeongyulgori.post.domain.post.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import personal.yeongyulgori.post.domain.image.model.dto.ImageRequestDto;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostRequestDto {

    @ApiModelProperty(value = "게시물 내용", example = "안녕하세요?")
    @NotNull
    private String content;

    @ApiModelProperty(value = "게시물에 포함되는 이미지들")
    private List<ImageRequestDto> imageRequestDtos;

    @ApiModelProperty(value = "이미지 갯수")
    public int getImageCount() {
        return (imageRequestDtos != null) ? imageRequestDtos.size() : 0;
    }

}
