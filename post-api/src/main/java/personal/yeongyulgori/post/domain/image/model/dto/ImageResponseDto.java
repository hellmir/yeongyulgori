package personal.yeongyulgori.post.domain.image.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import personal.yeongyulgori.post.domain.image.model.entity.Image;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageResponseDto {

    private Long id;
    private String name;
    private String url;
    private int positionInContent;

    public static ImageResponseDto from(Image image) {

        return ImageResponseDto.builder()
                .id(image.getId())
                .name(image.getName())
                .url(image.getUrl())
                .positionInContent(image.getPositionInContent())
                .build();

    }
}
