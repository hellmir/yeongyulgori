package personal.yeongyulgori.post.domain.post.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import personal.yeongyulgori.post.domain.image.model.dto.ImageResponseDto;
import personal.yeongyulgori.post.domain.post.model.entity.Post;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponseDto {

    private Long id;
    private Long userId;
    private String content;
    private List<ImageResponseDto> imageResponseDtos;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static PostResponseDto from(Post post) {

        return PostResponseDto.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .content(post.getContent())
                .imageResponseDtos(post.getImages() != null
                        ? post.getImages().stream().map(ImageResponseDto::from)
                        .collect(Collectors.toList())
                        : null)
                .createdAt(post.getCreatedAt())
                .modifiedAt(post.getModifiedAt())
                .build();

    }

}
