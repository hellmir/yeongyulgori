package personal.yeongyulgori.post.domain.post.model.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import personal.yeongyulgori.post.domain.image.model.entity.Image;
import personal.yeongyulgori.post.domain.image.model.repository.ImageRepository;
import personal.yeongyulgori.post.domain.post.model.entity.Post;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static personal.yeongyulgori.post.domain.image.model.constant.TargetType.POST;
import static testutil.TestObjectFactory.createImages;
import static testutil.TestObjectFactory.createPostWithoutIdAndImages;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
class PostRepositoryTest {

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private EntityManager entityManager;

    @DisplayName("게시물 ID와 사용자 ID를 통해 게시물을 조회할 수 있다.")
    @ParameterizedTest
    @CsvSource({
            "2, 안녕하세요?",
            "5, abcde",
            "41, 가나다라 12345 aBc 가나다 ab 123"
    })
    void findByPostIdAndUserId(Long userId, String content) {

        // given
        postRepository.save(createPostWithoutIdAndImages(1L, "abc"));
        Post savedPost = postRepository.save(createPostWithoutIdAndImages(userId, content));
        postRepository.save(createPostWithoutIdAndImages(7L, "abcd"));

        List<Image> images = createImages(savedPost.getId(), POST);

        imageRepository.saveAll(images);

        entityManager.refresh(savedPost);

        // when
        Post foundPost = postRepository.findByIdAndUserId(savedPost.getId(), savedPost.getUserId()).get();

        // then
        assertThat(foundPost.getId()).isEqualTo(savedPost.getId());
        assertThat(foundPost.getUserId()).isEqualTo(savedPost.getUserId());
        assertThat(foundPost.getContent()).isEqualTo(savedPost.getContent());

        assertThat(foundPost.getCreatedAt().truncatedTo(ChronoUnit.MILLIS))
                .isEqualTo(savedPost.getCreatedAt().truncatedTo(ChronoUnit.MILLIS));
        assertThat(foundPost.getModifiedAt().truncatedTo(ChronoUnit.MILLIS))
                .isEqualTo(savedPost.getModifiedAt().truncatedTo(ChronoUnit.MILLIS));

        assertThat(foundPost.getImages()).hasSize(3)
                .extracting("targetId", "name", "url")
                .containsExactly(
                        tuple(savedPost.getId(), "test_image1",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample1.jpg"),
                        tuple(savedPost.getId(), "test_image2",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample2.jpg"),
                        tuple(savedPost.getId(), "test_image3",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample3.jpg")
                );

    }

    @DisplayName("존재하지 않는 게시물 ID와 사용자 ID를 통해 게시물을 조회하면 게시물을 반환하지 않는다.")
    @Test
    void findByNonExistentPostIdOrUserId() {

        // given
        Post post = postRepository.save(createPostWithoutIdAndImages(1L, "abcde"));

        // when
        Optional<Post> foundPost1 = postRepository.findByIdAndUserId(post.getId() + 1, post.getUserId());
        Optional<Post> foundPost2 = postRepository.findByIdAndUserId(post.getId() + 1, post.getUserId());
        Optional<Post> foundPost3 = postRepository.findByIdAndUserId(post.getId() + 1, post.getUserId());


        // then
        assertThat(foundPost1).isEmpty();
        assertThat(foundPost2).isEmpty();
        assertThat(foundPost3).isEmpty();

    }

    @DisplayName("사용자 ID를 통해 해당 사용자의 게시물 목록을 조회할 수 있다.")
    @Test
    void findAllByUserId() {

        // given
        Post post1 = createPostWithoutIdAndImages(1L, "abcde");
        Post post2 = createPostWithoutIdAndImages(2L, "안녕하세요?");
        Post post3 = createPostWithoutIdAndImages(1L, "가나다라 12345 aBc 가나다 ab 123");

        postRepository.saveAll(List.of(post1, post2, post3));

        List<Image> images1 = createImages(post1.getId(), POST);
        List<Image> images2 = createImages(post2.getId(), POST);
        List<Image> images3 = createImages(post3.getId(), POST);

        imageRepository.saveAll(images1);
        imageRepository.saveAll(images2);
        imageRepository.saveAll(images3);

        entityManager.refresh(post3);

        // when
        Page<Post> foundPosts = postRepository.findAllByUserId(1L, Pageable.unpaged());

        // then
        assertThat(foundPosts).hasSize(2)
                .extracting("id", "userId", "content")
                .containsExactlyInAnyOrder(
                        tuple(post1.getId(), 1L, "abcde"),
                        tuple(post3.getId(), 1L, "가나다라 12345 aBc 가나다 ab 123")
                );

        assertThat(foundPosts.getContent().get(1).getImages()).hasSize(3)
                .extracting("targetId", "name", "url")
                .containsExactlyInAnyOrder(
                        tuple(post3.getId(), "test_image1",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample1.jpg"),
                        tuple(post3.getId(), "test_image2",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample2.jpg"),
                        tuple(post3.getId(), "test_image3",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample3.jpg")
                );

    }

    @DisplayName("사용자 ID를 통해 해당 사용자의 게시물 목록을 페이징 처리해 조회할 수 있다.")
    @Test
    void findAllByUserIdWithPaging() {

        // given
        Post post1 = createPostWithoutIdAndImages(1L, "abcde");
        Post post2 = createPostWithoutIdAndImages(2L, "안녕하세요?");
        Post post3 = createPostWithoutIdAndImages(1L, "가나다라abc마바 123454321 aBc 가나다 ab 123");
        Post post4 = createPostWithoutIdAndImages(1L, "가나다라 12345 aBc 가나다 ab 123");

        postRepository.saveAll(List.of(post1, post2, post3, post4));

        List<Image> images1 = createImages(post1.getId(), POST);
        List<Image> images2 = createImages(post2.getId(), POST);
        List<Image> images3 = createImages(post3.getId(), POST);
        List<Image> images4 = createImages(post4.getId(), POST);

        imageRepository.saveAll(images1);
        imageRepository.saveAll(images2);
        imageRepository.saveAll(images3);
        imageRepository.saveAll(images4);

        entityManager.refresh(post3);

        Pageable pageable = PageRequest.of(0, 2);

        // when
        Page<Post> foundPosts = postRepository.findAllByUserId(1L, pageable);

        // then
        assertThat(foundPosts).hasSize(2)
                .extracting("id", "userId", "content")
                .containsExactlyInAnyOrder(
                        tuple(post1.getId(), 1L, "abcde"),
                        tuple(post3.getId(), 1L, "가나다라abc마바 123454321 aBc 가나다 ab 123")
                );

        assertThat(foundPosts.getContent().get(1).getImages()).hasSize(3)
                .extracting("targetId", "name", "url")
                .containsExactlyInAnyOrder(
                        tuple(post3.getId(), "test_image1",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample1.jpg"),
                        tuple(post3.getId(), "test_image2",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample2.jpg"),
                        tuple(post3.getId(), "test_image3",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample3.jpg")
                );

    }

    @DisplayName("키워드를 포함하는 게시물 목록을 조회할 수 있다.")
    @Test
    void findByContentContainingIgnoreCase() {

        // given
        Post post1 = createPostWithoutIdAndImages(1L, "abcde");
        Post post2 = createPostWithoutIdAndImages(2L, "안녕하세요? 3212");
        Post post3 = createPostWithoutIdAndImages(2L, "안녕하세요?");
        Post post4 = createPostWithoutIdAndImages(1L, "가나다라abc마바 123454321 aBc 가나다 ab 123");
        Post post5 = createPostWithoutIdAndImages(1L, "가나다라 12345 aBc 가나다 ab 123");

        postRepository.saveAll(List.of(post1, post2, post3, post4, post5));

        List<Image> images1 = createImages(post1.getId(), POST);
        List<Image> images2 = createImages(post2.getId(), POST);
        List<Image> images3 = createImages(post3.getId(), POST);
        List<Image> images4 = createImages(post4.getId(), POST);
        List<Image> images5 = createImages(post5.getId(), POST);

        imageRepository.saveAll(images1);
        imageRepository.saveAll(images2);
        imageRepository.saveAll(images3);
        imageRepository.saveAll(images4);
        imageRepository.saveAll(images5);

        entityManager.refresh(post5);

        // when
        Page<Post> foundPosts = postRepository.findByContentContainingIgnoreCase("12", Pageable.unpaged());

        // then
        assertThat(foundPosts).hasSize(3)
                .extracting("id", "userId", "content")
                .containsExactlyInAnyOrder(
                        tuple(post2.getId(), 2L, "안녕하세요? 3212"),
                        tuple(post4.getId(), 1L, "가나다라abc마바 123454321 aBc 가나다 ab 123"),
                        tuple(post5.getId(), 1L, "가나다라 12345 aBc 가나다 ab 123")
                );

        assertThat(foundPosts.getContent().get(2).getImages()).hasSize(3)
                .extracting("targetId", "name", "url")
                .containsExactlyInAnyOrder(
                        tuple(post5.getId(), "test_image1",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample1.jpg"),
                        tuple(post5.getId(), "test_image2",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample2.jpg"),
                        tuple(post5.getId(), "test_image3",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample3.jpg")
                );

    }

    @DisplayName("키워드를 포함하는 게시물 목록을 페이징 처리해 조회할 수 있다.")
    @Test
    void findByContentContainingWithPaging() {

        // given
        Post post1 = createPostWithoutIdAndImages(1L, "abcde");
        Post post2 = createPostWithoutIdAndImages(2L, "안녕하세요? 3212");
        Post post3 = createPostWithoutIdAndImages(1L, "가나다라abc마바 123454321 aBc 가나다 ab 123");
        Post post4 = createPostWithoutIdAndImages(2L, "안녕하세요?");
        Post post5 = createPostWithoutIdAndImages(1L, "가나다라 12345 aBc 가나다 ab 123 안녕하세요?");

        postRepository.saveAll(List.of(post1, post2, post3, post4, post5));

        List<Image> images1 = createImages(post1.getId(), POST);
        List<Image> images2 = createImages(post2.getId(), POST);
        List<Image> images3 = createImages(post3.getId(), POST);
        List<Image> images4 = createImages(post4.getId(), POST);
        List<Image> images5 = createImages(post5.getId(), POST);

        imageRepository.saveAll(images1);
        imageRepository.saveAll(images2);
        imageRepository.saveAll(images3);
        imageRepository.saveAll(images4);
        imageRepository.saveAll(images5);

        entityManager.refresh(post4);

        Pageable pageable = PageRequest.of(0, 2);

        // when
        Page<Post> foundPosts = postRepository.findByContentContainingIgnoreCase("녕하세", pageable);

        // then
        assertThat(foundPosts).hasSize(2)
                .extracting("id", "userId", "content")
                .containsExactlyInAnyOrder(
                        tuple(post2.getId(), 2L, "안녕하세요? 3212"),
                        tuple(post4.getId(), 2L, "안녕하세요?")
                );

        assertThat(foundPosts.getContent().get(1).getImages()).hasSize(3)
                .extracting("targetId", "name", "url")
                .containsExactlyInAnyOrder(
                        tuple(post4.getId(), "test_image1",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample1.jpg"),
                        tuple(post4.getId(), "test_image2",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample2.jpg"),
                        tuple(post4.getId(), "test_image3",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample3.jpg")
                );

    }

    @DisplayName("해당 사용자의 게시물이 존재하면 true를 반환하고, 존재하지 않으면 false를 반환한다.")
    @Test
    void existsByUserId() {

        // given
        Post post1 = createPostWithoutIdAndImages(2L, "abcde");
        Post post2 = createPostWithoutIdAndImages(8L, "abcdef");
        Post post3 = createPostWithoutIdAndImages(7L, "가나다라abc마바 123454321 aBc 가나다 ab 123");
        Post post4 = createPostWithoutIdAndImages(8L, "가나다라 12345 aBc 가나다 ab 123");

        postRepository.saveAll(List.of(post1, post2, post3, post4));

        // when
        boolean isExist1 = postRepository.existsByUserId(2L);
        boolean isExist2 = postRepository.existsByUserId(8L);
        boolean isExist3 = postRepository.existsByUserId(1L);

        // then
        assertThat(isExist1).isTrue();
        assertThat(isExist2).isTrue();
        assertThat(isExist3).isFalse();

    }

}