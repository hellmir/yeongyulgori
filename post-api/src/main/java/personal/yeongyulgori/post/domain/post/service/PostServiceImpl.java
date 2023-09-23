package personal.yeongyulgori.post.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import personal.yeongyulgori.post.domain.image.service.ImageService;
import personal.yeongyulgori.post.domain.post.model.dto.PostRequestDto;
import personal.yeongyulgori.post.domain.post.model.dto.PostResponseDto;
import personal.yeongyulgori.post.domain.post.model.entity.Post;
import personal.yeongyulgori.post.domain.post.model.repository.PostRepository;
import personal.yeongyulgori.post.exception.serious.sub.InconsistentUserException;
import personal.yeongyulgori.post.exception.serious.sub.NonExistentPostException;
import personal.yeongyulgori.post.exception.serious.sub.UserPostsNotFound;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.transaction.annotation.Isolation.*;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final ImageService imageService;

    private final EntityManager entityManager;

    @Override
    @Transactional(isolation = READ_UNCOMMITTED, timeout = 20)
    public PostResponseDto registerPost(Long userId, PostRequestDto postRequestDto) {

        Post savedPost = postRepository.save
                (Post.of(userId, postRequestDto));

        if (postRequestDto.getImageRequestDtos() != null) {
            imageService.saveImages(savedPost.getId(), postRequestDto.getImageRequestDtos());
        }

        entityManager.refresh(savedPost);

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
        Post post = postRepository.findByIdAndUserId(id, writerId)
                .orElseThrow(() -> new EntityNotFoundException
                        ("해당하는 게시물을 찾을 수 없습니다. postId: " + id + ", writerId: " + writerId));

        return PostResponseDto.from(post);

    }

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true, timeout = 30)
    public Page<PostResponseDto> getAllUserPosts(Long userId, Pageable pageable) {

        Page<Post> posts = postRepository.findAllByUserId(userId, pageable);

        List<PostResponseDto> userResponseDtos = posts.getContent().stream()
                .map(PostResponseDto::from).collect(Collectors.toList());

        return new PageImpl<>(userResponseDtos, pageable, posts.getTotalElements());

    }

    @Override
    public Page<PostResponseDto> getSearchedPosts(String keyword, Pageable pageable) {

        Page<Post> posts = postRepository.findByContentContainingIgnoreCase(keyword, pageable);

        List<PostResponseDto> userResponseDtos = posts.getContent().stream()
                .map(PostResponseDto::from).collect(Collectors.toList());

        return new PageImpl<>(userResponseDtos, pageable, posts.getTotalElements());

    }

    @Override
    @Transactional(isolation = REPEATABLE_READ, timeout = 30)
    public PostResponseDto updatePost(Long id, Long userId, PostRequestDto postRequestDto) {

        Post post = validateUserIdAndPostId(userId, id);

        Post updatedPost = postRepository.save(post.updateFrom(postRequestDto));

        if (postRequestDto.getImageRequestDtos() != null) {
            imageService.updateImages(updatedPost.getId(), postRequestDto.getImageRequestDtos());
        } else {
            imageService.deleteAllImages(updatedPost.getId());
        }

        entityManager.flush();
        entityManager.refresh(updatedPost);

        return PostResponseDto.from(updatedPost);

    }

    @Override
    @Transactional(isolation = REPEATABLE_READ, timeout = 15)
    public void deletePost(Long id, Long userId) {

        validateUserIdAndPostId(userId, id);

        postRepository.deleteById(id);

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

            validateUserIdIsExists(userId);

            throw new InconsistentUserException
                    ("일치하지 않는 사용자입니다. postId: " + id +
                            ", 등록한 userId: " + post.getUserId() + ", 요청 userId: " + userId);

        }

        return post;

    }

    private void validateUserIdIsExists(Long userId) {

        boolean isUserIdExists = postRepository.existsByUserId(userId);

        if (!isUserIdExists) {
            throw new UserPostsNotFound("해당 사용자의 게시물이 존재하지 않습니다. userId: " + userId);
        }

    }

}
