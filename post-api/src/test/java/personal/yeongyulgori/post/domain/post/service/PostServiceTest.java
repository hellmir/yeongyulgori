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
import personal.yeongyulgori.post.domain.post.model.dto.PostRequestDto;
import personal.yeongyulgori.post.domain.post.model.dto.PostResponseDto;
import personal.yeongyulgori.post.domain.post.model.entity.Post;
import personal.yeongyulgori.post.domain.post.model.repository.PostRepository;
import personal.yeongyulgori.post.exception.serious.sub.InconsistentUserException;
import personal.yeongyulgori.post.exception.serious.sub.NonExistentPostException;
import personal.yeongyulgori.post.exception.serious.sub.UserPostsNotFound;

import javax.persistence.EntityNotFoundException;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.*;

@ActiveProfiles("test")
@SpringBootTest
class PostServiceTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @AfterEach
    void tearDown() {
        postRepository.deleteAll();
    }

    @DisplayName("사용자 ID와 게시물 내용을 전송해 게시물을 등록할 수 있다.")
    @ParameterizedTest
    @CsvSource({
            "2, 안녕하세요?",
            "5, abcde",
            "41, 가나다라 12345 aBc 가나다 ab 123"
    })
    void registerPost(Long userId, String content) {

        // given
        PostRequestDto postRegisterDto = PostRequestDto.builder()
                .content(content)
                .build();

        // when
        PostResponseDto postResponseDto = postService.registerPost(userId, postRegisterDto);

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

    @DisplayName("게시물 ID를 전송해 게시물을 조회할 수 있다.")
    @ParameterizedTest
    @CsvSource({
            "안녕하세요?",
            "abcde",
            "가나다라 12345 aBc 가나다 ab 123"
    })
    void getPost(String content) {

        // given
        postRepository.save(Post.of(1L, "abc"));
        Post post = postRepository.save(Post.of(3L, content));
        postRepository.save(Post.of(7L, "abcd"));

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

    }

    @DisplayName("잘못된 게시물 ID 또는 사용자 ID를 전송하면 EntityNotFoundException이 발생한다.")
    @Test
    void getPostByNonExistentPostId() {

        // given
        Post post = postRepository.save(Post.of(5L, "abcde"));

        // when, then
        assertThatThrownBy(() -> postService.getPost((post.getId() + 1), post.getUserId()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당하는 게시물을 찾을 수 없습니다. postId: "+ (post.getId() + 1)
                        + ", writerId: " + post.getUserId());

        assertThatThrownBy(() -> postService.getPost(post.getId(), (post.getUserId() + 1)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당하는 게시물을 찾을 수 없습니다. postId: "+ post.getId()
                        + ", writerId: " + (post.getUserId() + 1));

    }

    @DisplayName("사용자 ID를 전송해 해당 사용자의 게시물 목록을 페이징 처리해 조회할 수 있다.")
    @Test
    void getAllUserPosts() {

        // given
        Post post1 = Post.of(1L, "abcde");
        Post post2 = Post.of(2L, "안녕하세요?");
        Post post3 = Post.of(1L, "가나다라abc마바 123454321 aBc 가나다 ab 123");
        Post post4 = Post.of(1L, "가나다라 12345 aBc 가나다 ab 123");

        postRepository.saveAll(List.of(post1, post2, post3, post4));

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

    }

    @DisplayName("키워드를 전송해 키워드를 포함하는 게시물 목록을 페이징 처리해 조회할 수 있다.")
    @Test
    void getSearchedPosts() {

        // given
        Post post1 = Post.of(1L, "abcde");
        Post post2 = Post.of(2L, "안녕하세요? 3212");
        Post post3 = Post.of(1L, "가나다라abc마바 123454321 aBc 가나다 ab 123");
        Post post4 = Post.of(2L, "안녕하세요?");
        Post post5 = Post.of(1L, "가나다라 12345 aBc 가나다 ab 123 안녕하세요?");

        postRepository.saveAll(List.of(post1, post2, post3, post4, post5));

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


    }

    @DisplayName("게시물 ID와 사용자 ID를 전송해 해당 게시물을 수정할 수 있다.")
    @ParameterizedTest
    @CsvSource({
            "2, 안녕하세요?, 안뇽하세요?",
            "5, abcde, 가나다라abc마바 123454321 aBc 가나다 ab 123",
            "41, 가나다라 12345 aBc 가나다 ab 123, 안냥하세요?"
    })
    void updatePost(Long userId, String content, String contentToUpdate) throws InterruptedException {

        // given
        Post post = postRepository.save(Post.of(userId, content));

        PostRequestDto postRequestDto = PostRequestDto.builder()
                .content(contentToUpdate)
                .build();

        // when
        PostResponseDto postResponseDto = postService.updatePost(post.getId(), post.getUserId(), postRequestDto);

        Post updatedPost = postRepository.findById(post.getId()).get();

        // then
        assertThat(postResponseDto.getContent()).isEqualTo(contentToUpdate);
        assertThat(updatedPost.getContent()).isEqualTo(contentToUpdate);

        assertThat(postResponseDto.getCreatedAt().truncatedTo(ChronoUnit.MILLIS))
                .isEqualTo(post.getCreatedAt().truncatedTo(ChronoUnit.MILLIS));

        assertThat(postResponseDto.getModifiedAt().truncatedTo(ChronoUnit.MILLIS))
                .isNotEqualTo(post.getModifiedAt().truncatedTo(ChronoUnit.MILLIS));

    }

    @DisplayName("일치하지 않는 사용자 또는 게시물 정보를 전송해 게시물을 수정하려 하면 예외가 발생한다.")
    @Test
    void updatePostWithNonExistentUserIdOrPostId() {

        // given
        Post post1 = Post.of(2L, "abcde");
        Post post2 = Post.of(8L, "abcdef");
        Post post3 = Post.of(7L, "가나다라abc마바 123454321 aBc 가나다 ab 123");
        Post post4 = Post.of(8L, "가나다라 12345 aBc 가나다 ab 123");

        postRepository.saveAll(List.of(post1, post2, post3, post4));

        PostRequestDto postRequestDto1 = PostRequestDto.builder()
                .content("abc")
                .build();

        PostRequestDto postRequestDto2 = PostRequestDto.builder()
                .content("abc")
                .build();

        PostRequestDto postRequestDto3 = PostRequestDto.builder()
                .content("abc")
                .build();


        // when, then
        assertThatThrownBy(() -> postService.updatePost((post4.getId() + 1), 8L, postRequestDto2))
                .isInstanceOf(NonExistentPostException.class)
                .hasMessage("해당 게시물을 찾을 수 없습니다. postId: " + (post4.getId() + 1));

        assertThatThrownBy(() -> postService.updatePost(post3.getId(), 5L, postRequestDto1))
                .isInstanceOf(UserPostsNotFound.class)
                .hasMessage("해당 사용자의 게시물이 존재하지 않습니다. userId: " + 5L);

        assertThatThrownBy(() -> postService.updatePost((post4.getId()), 7L, postRequestDto3))
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
        postRepository.save(Post.of(10L, "abc"));
        Post post = postRepository.save(Post.of(userId, content));
        postRepository.save(Post.of(17L, "abcd"));

        // when
        postService.deletePost(post.getId(), userId);

        // then
        assertThat(postRepository.findById(post.getId())).isNotPresent();

    }

    @DisplayName("일치하지 않는 사용자 또는 게시물 정보를 전송해 게시물을 삭제하려 하면 예외가 발생한다.")
    @Test
    void deletePostWithNonExistentUserIdOrPostId() {

        // given
        Post post1 = Post.of(2L, "abcde");
        Post post2 = Post.of(8L, "abcdef");
        Post post3 = Post.of(7L, "가나다라abc마바 123454321 aBc 가나다 ab 123");
        Post post4 = Post.of(8L, "가나다라 12345 aBc 가나다 ab 123");

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