package personal.yeongyulgori.post.domain.post.model.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import personal.yeongyulgori.post.base.BaseEntity;

import javax.persistence.*;
import java.time.LocalDateTime;

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

    @Builder
    private Post(Long id, Long userId, String content, LocalDateTime createdAt, LocalDateTime modifiedAt) {

        this.id = id;
        this.userId = userId;
        this.content = content;

        setCreatedAt(createdAt);

        setModifiedAt(modifiedAt);

    }

    public static Post of(Long userId, String content) {

        return Post.builder()
                .userId(userId)
                .content(content)
                .build();

    }

    public Post of(String content) {

        return Post.builder()
                .id(id)
                .userId(userId)
                .content(content)
                .createdAt(getCreatedAt())
                .modifiedAt(LocalDateTime.now())
                .build();

    }

}
