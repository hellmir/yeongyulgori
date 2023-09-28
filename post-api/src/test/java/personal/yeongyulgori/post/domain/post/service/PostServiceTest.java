package personal.yeongyulgori.post.domain.post.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.transaction.annotation.Transactional;
import personal.yeongyulgori.post.domain.image.model.dto.ImageRequestDto;
import personal.yeongyulgori.post.domain.image.model.entity.Image;
import personal.yeongyulgori.post.domain.image.model.repository.ImageRepository;
import personal.yeongyulgori.post.domain.post.model.dto.PostRequestDto;
import personal.yeongyulgori.post.domain.post.model.dto.PostResponseDto;
import personal.yeongyulgori.post.domain.post.model.entity.Post;
import personal.yeongyulgori.post.domain.post.model.repository.PostRepository;
import personal.yeongyulgori.post.exception.serious.sub.InconsistentUserException;
import personal.yeongyulgori.post.exception.serious.sub.NonExistentPostException;
import personal.yeongyulgori.post.exception.serious.sub.UserPostsNotFound;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static personal.yeongyulgori.post.domain.image.model.constant.TargetType.POST;
import static testutil.TestObjectFactory.*;

@ActiveProfiles("test")
@SpringBootTest
class PostServiceTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private EntityManager entityManager;

    @AfterEach
    void tearDown() {
        postRepository.deleteAll();
    }

    @DisplayName("사용자 ID와 게시물 내용을 전송해 이미지가 없는 게시물을 등록할 수 있다.")
    @ParameterizedTest
    @CsvSource({
            "2, 안녕하세요?",
            "5, abcde",
            "41, 가나다라 12345 aBc 가나다 ab 123"
    })
    void registerPost(Long userId, String content) {

        // given
        PostRequestDto postRequestDto = createPostRequestDto(content);

        // when
        PostResponseDto postResponseDto = postService.registerPost(userId, postRequestDto);

        Post post = postRepository.findById(postResponseDto.getId()).get();

        // then
        assertThat(post.getUserId()).isEqualTo(userId);
        assertThat(post.getContent()).isEqualTo(content);

        assertThat(postResponseDto.getUserId()).isEqualTo(userId);
        assertThat(postResponseDto.getContent()).isEqualTo(content);

        assertThat(postResponseDto.getCreatedAt().truncatedTo(ChronoUnit.MILLIS))
                .isEqualTo(post.getCreatedAt().truncatedTo(ChronoUnit.MILLIS));
        assertThat(postResponseDto.getModifiedAt().truncatedTo(ChronoUnit.MILLIS))
                .isEqualTo(post.getModifiedAt().truncatedTo(ChronoUnit.MILLIS));

        assertThat(post.getModifiedAt()).isEqualTo(post.getCreatedAt());

    }

    @DisplayName("사용자 ID와 게시물 내용, 이미지들을 전송해 게시물과 내용, 포함된 이미지들을 등록할 수 있다.")
    @ParameterizedTest
    @CsvSource({
            "2, 안녕하세요?",
            "5, abcde",
            "41, 가나다라 12345 aBc 가나다 ab 123"
    })
    // Post에서 Image를 로딩하는 fetch type이 LAZY이므로, 영속성 컨텍스트를 유지해서 LazyInitializationException 방지
    @Transactional
    void registerPostWithImages(Long userId, String content) {

        // given
        List<ImageRequestDto> imageRequestDtos = createImageRequestDtos(POST);

        PostRequestDto postRequestDto = createPostRequestDto(content, imageRequestDtos);

        // when
        PostResponseDto postResponseDto = postService.registerPost(userId, postRequestDto);

        Post post = postRepository.findById(postResponseDto.getId()).get();

        // then
        assertThat(post.getUserId()).isEqualTo(userId);
        assertThat(post.getContent()).isEqualTo(content);

        assertThat(postResponseDto.getUserId()).isEqualTo(userId);
        assertThat(postResponseDto.getContent()).isEqualTo(content);

        assertThat(postResponseDto.getImageResponseDtos()).hasSize(3)
                .extracting("name", "url", "positionInContent")
                .containsExactlyInAnyOrder(
                        tuple("test_image1",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample1.jpg", 10),
                        tuple("test_image2",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample2.jpg", 15),
                        tuple("test_image3",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample3.jpg", 18)
                );

        assertThat(postResponseDto.getCreatedAt().truncatedTo(ChronoUnit.MILLIS))
                .isEqualTo(post.getCreatedAt().truncatedTo(ChronoUnit.MILLIS));
        assertThat(postResponseDto.getModifiedAt().truncatedTo(ChronoUnit.MILLIS))
                .isEqualTo(post.getModifiedAt().truncatedTo(ChronoUnit.MILLIS));

        assertThat(post.getModifiedAt()).isEqualTo(post.getCreatedAt());

        assertThat(post.getImages()).hasSize(3)
                .extracting("targetType", "name", "url", "positionInContent")
                .containsExactlyInAnyOrder(
                        tuple(POST, "test_image1",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample1.jpg", 10),
                        tuple(POST, "test_image2",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample2.jpg", 15),
                        tuple(POST, "test_image3",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample3.jpg", 18)
                );

        assertThat(postResponseDto.getImageResponseDtos()).hasSize(3)
                .extracting("name", "url", "positionInContent")
                .containsExactlyInAnyOrder(
                        tuple("test_image1",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample1.jpg", 10),
                        tuple("test_image2",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample2.jpg", 15),
                        tuple("test_image3",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample3.jpg", 18)
                );

    }

    @DisplayName("게시물 ID를 전송해 게시물의 내용과 이미지들을 조회할 수 있다.")
    @ParameterizedTest
    @CsvSource({
            "안녕하세요?",
            "abcde",
            "가나다라 12345 aBc 가나다 ab 123"
    })
    void getPost(String content) {

        // given
        postRepository.save(createPostWithoutIdAndImages(1L, "abc"));
        Post post = postRepository.save(createPostWithoutIdAndImages(3L, content));
        postRepository.save(createPostWithoutIdAndImages(7L, "abcd"));

        List<Image> images = createImages(post.getId(), POST);

        imageRepository.saveAll(images);

        // when
        PostResponseDto postResponseDto = postService.getPost(post.getId(), post.getUserId());

        // then
        assertThat(postResponseDto.getId()).isEqualTo(post.getId());
        assertThat(postResponseDto.getUserId()).isEqualTo(post.getUserId());
        assertThat(postResponseDto.getContent()).isEqualTo(post.getContent());

        assertThat(postResponseDto.getCreatedAt().truncatedTo(ChronoUnit.MILLIS))
                .isEqualTo(post.getCreatedAt().truncatedTo(ChronoUnit.MILLIS));

        assertThat(postResponseDto.getModifiedAt().truncatedTo(ChronoUnit.MILLIS))
                .isEqualTo(post.getModifiedAt().truncatedTo(ChronoUnit.MILLIS));

        assertThat(postResponseDto.getImageResponseDtos()).hasSize(3)
                .extracting("name", "url")
                .containsExactlyInAnyOrder(
                        tuple("test_image1",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample1.jpg"),
                        tuple("test_image2",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample2.jpg"),
                        tuple("test_image3",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample3.jpg")
                );

    }

    @DisplayName("잘못된 게시물 ID 또는 사용자 ID를 전송하면 EntityNotFoundException이 발생한다.")
    @Test
    void getPostByNonExistentPostId() {

        // given
        PostRequestDto postRequestDto = createPostRequestDto("abcde", createImageRequestDtos(POST));
        Post post = postRepository.save(Post.of(5L, postRequestDto));

        // when, then
        assertThatThrownBy(() -> postService.getPost((post.getId() + 1), post.getUserId()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당하는 게시물을 찾을 수 없습니다. postId: " + (post.getId() + 1)
                        + ", writerId: " + post.getUserId());

        assertThatThrownBy(() -> postService.getPost(post.getId(), (post.getUserId() + 1)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당하는 게시물을 찾을 수 없습니다. postId: " + post.getId()
                        + ", writerId: " + (post.getUserId() + 1));

    }

    @DisplayName("사용자 ID를 전송해 해당 사용자의 게시물 목록을 페이징 처리해 조회할 수 있다.")
    @Test
    void getAllUserPosts() {

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

        Pageable pageable = PageRequest.of(0, 2);

        // when
        Page<PostResponseDto> postResponseDtos = postService.getAllUserPosts(1L, pageable);

        // then
        Assertions.assertThat(postResponseDtos).hasSize(2)
                .extracting("id", "userId", "content")
                .containsExactlyInAnyOrder(
                        tuple(post1.getId(), 1L, "abcde"),
                        tuple(post3.getId(), 1L, "가나다라abc마바 123454321 aBc 가나다 ab 123")
                );

        assertThat(postResponseDtos.getContent().get(1).getImageResponseDtos()).hasSize(3)
                .extracting("name", "url")
                .containsExactlyInAnyOrder(
                        tuple("test_image1",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample1.jpg"),
                        tuple("test_image2",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample2.jpg"),
                        tuple("test_image3",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample3.jpg")
                );

    }

    @DisplayName("키워드를 전송해 키워드를 포함하는 게시물 목록을 페이징 처리해 조회할 수 있다.")
    @Test
    // Post에서 Image를 로딩하는 fetch type이 LAZY이므로, 영속성 컨텍스트를 유지해서 LazyInitializationException 방지
    @Transactional
    void getSearchedPosts() {

        // given
        Post post1 = createPostWithoutIdAndImages(1L, "abcde");
        Post post2 = createPostWithoutIdAndImages(2L, "안녕하세요? 3212");
        Post post3 = createPostWithoutIdAndImages(1L, "가나다라abc마바 123454321 aBc 가나다 ab 123");
        Post post4 = createPostWithoutIdAndImages(1L, "안녕하세요?");
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

        entityManager.refresh(post3);

        Pageable pageable = PageRequest.of(0, 2);

        // when
        Page<PostResponseDto> postResponseDtos = postService.getSearchedPosts("Bc", pageable);

        // then
        Assertions.assertThat(postResponseDtos).hasSize(2)
                .extracting("id", "userId", "content")
                .containsExactlyInAnyOrder(
                        tuple(post1.getId(), 1L, "abcde"),
                        tuple(post3.getId(), 1L, "가나다라abc마바 123454321 aBc 가나다 ab 123")
                );

        assertThat(postResponseDtos.getContent().get(1).getImageResponseDtos()).hasSize(3)
                .extracting("name", "url")
                .containsExactlyInAnyOrder(
                        tuple("test_image1",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample1.jpg"),
                        tuple("test_image2",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample2.jpg"),
                        tuple("test_image3",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample3.jpg")
                );


    }

    @DisplayName("게시물 ID와 사용자 ID를 전송해 해당 게시물을 수정할 수 있다.")
    @ParameterizedTest
    @CsvSource({
            "2, 안녕하세요?, 안뇽하세요?",
            "5, abcde, 가나다라abc마바 123454321 aBc 가나다 ab 123",
            "41, 가나다라 12345 aBc 가나다 ab 123, 안냥하세요?"
    })
    // Post에서 Image를 로딩하는 fetch type이 LAZY이므로, 영속성 컨텍스트를 유지해서 LazyInitializationException 방지
    @Transactional
    void updatePost(Long userId, String content, String contentToUpdate) throws InterruptedException {

        // given
        Post post = postRepository.save(createPostWithoutIdAndImages(userId, content));

        createImage(post.getId(), POST, "test_image1",
                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample1.jpg", 10);

        createImage(post.getId(), POST, "test_image2",
                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample2.jpg", 12);

        createImage(post.getId(), POST, "test_image3",
                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample3.jpg", 15);

        createImage(post.getId(), POST, "test_image4",
                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample4.jpg", 18);

        createImage(post.getId(), POST, "test_image5",
                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample5.jpg", 23);

        imageRepository.saveAll(createImages(post.getId(), POST));

        ImageRequestDto imageRequestDto1 = createImageRequestDto
                (POST, "test_image2",
                        "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample2.jpg", 12);

        ImageRequestDto imageRequestDto2 = createImageRequestDto
                (POST, "test_image4",
                        "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample4.jpg", 18);

        ImageRequestDto imageRequestDto3 = createImageRequestDto
                (POST, "test_image6",
                        "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample6.jpg", 20);

        ImageRequestDto imageRequestDto4 = createImageRequestDto
                (POST, "test_image7",
                        "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample7.jpg", 25);

        List<ImageRequestDto> imageRequestDtos
                = List.of(imageRequestDto1, imageRequestDto2, imageRequestDto3, imageRequestDto4);

        PostRequestDto postRequestDto = createPostUpdateDto(contentToUpdate, imageRequestDtos);

        // when
        PostResponseDto postResponseDto = postService.updatePost(post.getId(), post.getUserId(), postRequestDto);

        Post updatedPost = postRepository.findById(post.getId()).get();

        // then
        assertThat(postResponseDto.getContent()).isEqualTo(contentToUpdate);
        assertThat(updatedPost.getContent()).isEqualTo(contentToUpdate);

        assertThat(postResponseDto.getCreatedAt().truncatedTo(ChronoUnit.MILLIS))
                .isEqualTo(post.getCreatedAt().truncatedTo(ChronoUnit.MILLIS));

        assertThat(postResponseDto.getModifiedAt().truncatedTo(ChronoUnit.MILLIS))
                .isNotEqualTo(postResponseDto.getCreatedAt().truncatedTo(ChronoUnit.MILLIS));

        assertThat(updatedPost.getModifiedAt().truncatedTo(ChronoUnit.MILLIS))
                .isNotEqualTo(updatedPost.getCreatedAt().truncatedTo(ChronoUnit.MILLIS));

        assertThat(postResponseDto.getImageResponseDtos()).hasSize(4)
                .extracting("name", "url", "positionInContent")
                .containsExactlyInAnyOrder(
                        tuple("test_image2",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample2.jpg", 12),
                        tuple("test_image4",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample4.jpg", 18),
                        tuple("test_image6",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample6.jpg", 20),
                        tuple("test_image7",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample7.jpg", 25)
                );


        assertThat(updatedPost.getImages()).hasSize(4)
                .extracting("targetType", "name", "url", "positionInContent")
                .containsExactlyInAnyOrder(
                        tuple(POST, "test_image2",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample2.jpg", 12),
                        tuple(POST, "test_image4",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample4.jpg", 18),
                        tuple(POST, "test_image6",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample6.jpg", 20),
                        tuple(POST, "test_image7",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample7.jpg", 25)
                );

    }

    @DisplayName("일치하지 않는 사용자 또는 게시물 정보를 전송해 게시물을 수정하려 하면 예외가 발생한다.")
    @Test
    void updatePostWithNonExistentUserIdOrPostId() {

        // given
        List<ImageRequestDto> imageRequestDtos = createImageRequestDtos(POST);

        PostRequestDto postRequestDto1
                = createPostRequestDto("abcde", imageRequestDtos);
        PostRequestDto postRequestDto2
                = createPostRequestDto("abcdef", imageRequestDtos);
        PostRequestDto postRequestDto3
                = createPostRequestDto("가나다라abc마바 123454321 aBc 가나다 ab 123", imageRequestDtos);
        PostRequestDto postRequestDto4
                = createPostRequestDto("가나다라 12345 aBc 가나다 ab 123", imageRequestDtos);

        Post post1 = Post.of(2L, postRequestDto1);
        Post post2 = Post.of(8L, postRequestDto2);
        Post post3 = Post.of(7L, postRequestDto3);
        Post post4 = Post.of(8L, postRequestDto4);

        postRepository.saveAll(List.of(post1, post2, post3, post4));

        PostRequestDto postUpdateDto1 = createPostUpdateDto("ab");
        PostRequestDto postUpdateDto2 = createPostUpdateDto("abc");
        PostRequestDto postUpdateDto3 = createPostUpdateDto("abcd");

        // when, then
        assertThatThrownBy(() -> postService.updatePost((post4.getId() + 1), 8L, postUpdateDto2))
                .isInstanceOf(NonExistentPostException.class)
                .hasMessage("해당 게시물을 찾을 수 없습니다. postId: " + (post4.getId() + 1));

        assertThatThrownBy(() -> postService.updatePost(post3.getId(), 5L, postUpdateDto1))
                .isInstanceOf(UserPostsNotFound.class)
                .hasMessage("해당 사용자의 게시물이 존재하지 않습니다. userId: " + 5L);

        assertThatThrownBy(() -> postService.updatePost((post4.getId()), 7L, postUpdateDto3))
                .isInstanceOf(InconsistentUserException.class)
                .hasMessage("일치하지 않는 사용자입니다. postId: " + post4.getId() +
                        ", 등록한 userId: " + post4.getUserId() + ", 요청 userId: " + 7L);

    }

    @DisplayName("사용자 ID와 게시물 ID를 전송해 해당 게시물을 삭제할 수 있다.")
    @ParameterizedTest
    @CsvSource({
            "2, 안녕하세요?",
            "5, abcde",
            "41, 가나다라 12345 aBc 가나다 ab 123"
    })
    void deletePost(Long userId, String content) {

        // given
        Post post1 = createPostWithoutIdAndImages(10L, "abcde");
        Post post2 = createPostWithoutIdAndImages(17L, content);
        Post post3 = createPostWithoutIdAndImages(21L, "가나다라abc마바 123454321 aBc 가나다 ab 123");
        Post post4 = createPostWithoutIdAndImages(userId, "안녕하세요?");
        Post post5 = createPostWithoutIdAndImages(5L, "가나다라 12345 aBc 가나다 ab 123");

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

        // when
        postService.deletePost(post4.getId(), userId);

        // then
        assertThat(postRepository.findById(post4.getId())).isNotPresent();
        assertThat(imageRepository.findAllByTargetIdOrderByIdAsc(post4.getId())).isEmpty();

    }

    @DisplayName("일치하지 않는 사용자 또는 게시물 정보를 전송해 게시물을 삭제하려 하면 예외가 발생한다.")
    @Test
    void deletePostWithNonExistentUserIdOrPostId() {

        // given
        List<ImageRequestDto> imageRequestDtos = createImageRequestDtos(POST);

        PostRequestDto postRequestDto1
                = createPostRequestDto("abcde", imageRequestDtos);
        PostRequestDto postRequestDto2
                = createPostRequestDto("abcdef", imageRequestDtos);
        PostRequestDto postRequestDto3
                = createPostRequestDto("가나다라abc마바 123454321 aBc 가나다 ab 123", imageRequestDtos);
        PostRequestDto postRequestDto4
                = createPostRequestDto("가나다라 12345 aBc 가나다 ab 123", imageRequestDtos);

        Post post1 = Post.of(2L, postRequestDto1);
        Post post2 = Post.of(8L, postRequestDto2);
        Post post3 = Post.of(7L, postRequestDto3);
        Post post4 = Post.of(8L, postRequestDto4);

        postRepository.saveAll(List.of(post1, post2, post3, post4));

        // when, then
        assertThatThrownBy(() -> postService.deletePost((post4.getId() + 1), 8L))
                .isInstanceOf(NonExistentPostException.class)
                .hasMessage("해당 게시물을 찾을 수 없습니다. postId: " + (post4.getId() + 1));

        assertThatThrownBy(() -> postService.deletePost(post3.getId(), 5L))
                .isInstanceOf(UserPostsNotFound.class)
                .hasMessage("해당 사용자의 게시물이 존재하지 않습니다. userId: " + 5L);

        assertThatThrownBy(() -> postService.deletePost((post4.getId()), 7L))
                .isInstanceOf(InconsistentUserException.class)
                .hasMessage("일치하지 않는 사용자입니다. postId: " + post4.getId() +
                        ", 등록한 userId: " + 8L + ", 요청 userId: " + 7L);

    }

}