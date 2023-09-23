package personal.yeongyulgori.post.domain.image.model.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import personal.yeongyulgori.post.domain.image.model.entity.Image;
import personal.yeongyulgori.post.domain.post.model.entity.Post;
import personal.yeongyulgori.post.domain.post.model.repository.PostRepository;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static personal.yeongyulgori.post.domain.image.model.constant.TargetType.POST;
import static testutil.TestObjectFactory.createImages;
import static testutil.TestObjectFactory.createPostWithoutIdAndImages;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class ImageRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private EntityManager entityManager;

    @DisplayName("입력된 postId를 가지는 게시물의 이미지 목록을 ID 오름차순으로 조회할 수 있다.")
    @Test
    void findAllByTargetIdOrderByIdAsc() {

        // given
        Post post = postRepository.save(createPostWithoutIdAndImages(3L, "abc"));
        List<Image> images = createImages(post.getId(), POST);
        imageRepository.saveAll(images);


        // when
        List<Image> foundImages = imageRepository.findAllByTargetIdOrderByIdAsc(post.getId());

        // then
        assertThat(foundImages).hasSize(3)
                .extracting("targetType", "name", "url", "positionInContent")
                .containsExactly(
                        tuple(POST, "test_image1", "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample1.jpg", 10),
                        tuple(POST, "test_image2", "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample2.jpg", 15),
                        tuple(POST, "test_image3", "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample3.jpg", 18)
                );

    }

    @DisplayName("입력된 postId를 가지는 게시물에 포함된 모든 이미지들을 삭제할 수 있다.")
    @Test
    void deleteAllByTargetId() {

        // given
        Post post = postRepository.save(createPostWithoutIdAndImages(3L, "abc"));
        List<Image> images = createImages(post.getId(), POST);
        imageRepository.saveAll(images);

        // when
        imageRepository.deleteAllByTargetId(post.getId());

        // then
        assertThat(imageRepository.findAllByTargetIdOrderByIdAsc(post.getId())).isEmpty();

    }

}