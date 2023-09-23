package personal.yeongyulgori.post.domain.post.model.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import personal.yeongyulgori.post.base.BaseEntity;
import personal.yeongyulgori.post.domain.image.model.entity.Image;
import personal.yeongyulgori.post.domain.post.model.dto.PostRequestDto;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity(name = "post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "post")
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(columnDefinition = "MEDIUMTEXT", nullable = false)
    private String content;

    @OneToMany(mappedBy = "targetId", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Where(clause = "target_type = 'POST'")
    private List<Image> images;

    private int imageCount;

    @Builder
    private Post(Long id, Long userId, String content, List<Image> images,
                 int imageCount, LocalDateTime createdAt) {

        this.id = id;
        this.userId = userId;
        this.content = content;
        this.images = images;
        this.imageCount = imageCount;

        setCreatedAt(createdAt);

    }

    public static Post of(Long userId, PostRequestDto postRequestDto) {

        return Post.builder()
                .userId(userId)
                .content(postRequestDto.getContent())
                .imageCount(postRequestDto.getImageCount())
                .build();

    }

    public Post updateFrom(PostRequestDto postRequestDto) {

        return Post.builder()
                .id(id)
                .userId(userId)
                .content(postRequestDto.getContent())
                .images(images)
                .imageCount(postRequestDto.getImageCount())
                .createdAt(getCreatedAt())
                .build();

    }

}
