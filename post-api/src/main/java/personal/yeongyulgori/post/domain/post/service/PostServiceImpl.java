package personal.yeongyulgori.post.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import personal.yeongyulgori.post.domain.post.model.dto.PostRequestDto;
import personal.yeongyulgori.post.domain.post.model.dto.PostResponseDto;
import personal.yeongyulgori.post.domain.post.model.entity.Post;
import personal.yeongyulgori.post.domain.post.model.repository.PostRepository;
import personal.yeongyulgori.post.exception.serious.sub.InconsistentUserException;
import personal.yeongyulgori.post.exception.serious.sub.NonExistentPostException;
import personal.yeongyulgori.post.exception.serious.sub.UserPostsNotFound;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Isolation.REPEATABLE_READ;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

    private static final Logger log = LoggerFactory.getLogger(PostService.class);

    @Override
    public PostResponseDto registerPost(Long userId, PostRequestDto postRegisterDto) {

        log.info("Beginning to register post by userId: {}", userId);

        Post savedPost = postRepository.save(Post.of(userId, postRegisterDto.getContent()));

        log.info("Post registered successfully by userId: {}", userId);

        return PostResponseDto.from(savedPost);

    }

    /**
     * 1. 성능이 중요한 메서드
     * 2. 예외가 발생한 경우 사용자가 탈퇴하거나 게시물이 삭제되었을 확률이 높음
     * 3. 게시자 ID가 일치하는지 여부의 중요성이 낮음
     * -> id와 writerId 동시 검증(log level: WARN)
     */
    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true, timeout = 10)
    public PostResponseDto getPost(Long id, Long writerId) {

        log.info("Beginning to get post by postId: {}, writerId: {}", id, writerId);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Post post = postRepository.findByIdAndUserId(id, writerId)
                .orElseThrow(() -> new EntityNotFoundException
                        ("해당하는 게시물을 찾을 수 없습니다. postId: " + id + ", writerId: " + writerId));

        stopWatch.stop();

        log.info("Post retrieved successfully by postId: {}, writerId: {}\n Retrieving task execution time: {} ms",
                id, writerId, stopWatch.getTotalTimeMillis());

        return PostResponseDto.from(post);

    }

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true, timeout = 30)
    public Page<PostResponseDto> getAllUserPosts(Long userId, Pageable pageable) {

        log.info("Beginning to get all posts by userId: {}", userId);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Page<Post> posts = postRepository.findAllByUserId(userId, pageable);

        List<PostResponseDto> userResponseDtos = posts.getContent().stream()
                .map(PostResponseDto::from).collect(Collectors.toList());

        stopWatch.stop();

        log.info("All posts retrieved successfully by userId: {}\n Retrieving task execution time: {} ms",
                userId, stopWatch.getTotalTimeMillis());

        return new PageImpl<>(userResponseDtos, pageable, posts.getTotalElements());

    }

    @Override
    public Page<PostResponseDto> getSearchedPosts(String keyword, Pageable pageable) {

        log.info("Beginning to get all searched posts by keyword: {}", keyword);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Page<Post> posts = postRepository.findByContentContainingIgnoreCase(keyword, pageable);

        List<PostResponseDto> userResponseDtos = posts.getContent().stream()
                .map(PostResponseDto::from).collect(Collectors.toList());

        stopWatch.stop();

        log.info("All searched posts retrieved successfully by keyword: {}\n Retrieving task execution time: {} ms",
                keyword, stopWatch.getTotalTimeMillis());

        return new PageImpl<>(userResponseDtos, pageable, posts.getTotalElements());

    }

    @Override
    @Transactional(isolation = REPEATABLE_READ, timeout = 15)
    public PostResponseDto updatePost(Long id, Long userId, PostRequestDto postRequestDto) {

        log.info("Beginning to update post by userId: {}, postId: {}", userId, id);

        boolean isUserIdExists = postRepository.existsByUserId(userId);

        Post post = validateUserIdAndPostId(userId, id);

        Post updatedPost = postRepository.save(post.of(postRequestDto.getContent()));

        log.info("Post updated successfully by userId: {}, postId: {}", userId, id);

        return PostResponseDto.from(updatedPost);

    }

    @Override
    @Transactional(isolation = REPEATABLE_READ, timeout = 15)
    public void deletePost(Long id, Long userId) {

        log.info("Beginning to delete post by userId: {}, postId: {}", userId, id);

        validateUserIdAndPostId(userId, id);

        postRepository.deleteById(id);

        log.info("Post deleted successfully by userId: {}, by postId: {}", userId, id);

    }

    /**
     * 1. 수정, 삭제는 조회처럼 자주 발생하지 않으므로 성능에 영향이 적음
     * 2. 데이터베이스의 문제로 예외가 발생하므로 즉시 조치 필요
     * 3. 사용자 ID가 일치하는지 여부의 중요성이 높음
     * -> 빠르게 디버깅할 수 있도록 id와 userId에 대해 별도 검증(log level: ERROR)
     */
    private Post validateUserIdAndPostId(Long userId, Long id) {

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NonExistentPostException
                        ("해당 게시물을 찾을 수 없습니다. postId: " + id));

        if (!post.getUserId().equals(userId)) {

            boolean isUserIdExists = postRepository.existsByUserId(userId);

            if (!isUserIdExists) {
                throw new UserPostsNotFound("해당 사용자의 게시물이 존재하지 않습니다. userId: " + userId);
            }

            throw new InconsistentUserException
                    ("일치하지 않는 사용자입니다. postId: " + id +
                            ", 등록한 userId: " + post.getUserId() + ", 요청 userId: " + userId);

        }

        return post;

    }

}
