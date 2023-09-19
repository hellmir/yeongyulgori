package testutil;

import personal.yeongyulgori.post.domain.post.model.entity.Post;

public class TestObjectFactory {

    public static Post createPost(Long id, Long userId, String content) {

        return Post.builder()
                .id(id)
                .userId(userId)
                .content(content)
                .build();

    }

}
