package personal.yeongyulgori.post.domain.image.model.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import personal.yeongyulgori.post.base.BaseEntity;
import personal.yeongyulgori.post.domain.image.model.constant.TargetType;
import personal.yeongyulgori.post.domain.image.model.dto.ImageRequestDto;

import javax.persistence.*;

@Entity(name = "image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "image")
public class Image extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TargetType targetType;

    @Column(length = 20)
    private String name;

    @Column(nullable = false)
    private String url;

    private int positionInContent;

    @Builder
    private Image(Long targetId, TargetType targetType, String name, String url, int positionInContent) {

        this.targetId = targetId;
        this.targetType = targetType;
        this.name = name;
        this.url = url;
        this.positionInContent = positionInContent;

    }

    public static Image from(Long targetId, ImageRequestDto imageRequestDto) {

        return Image.builder()
                .targetType(imageRequestDto.getTargetType())
                .targetId(targetId)
                .name(imageRequestDto.getName())
                .url(imageRequestDto.getUrl())
                .positionInContent(imageRequestDto.getPositionInContent())
                .build();

    }

}
