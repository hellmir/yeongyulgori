package personal.yeongyulgori.post.domain.post.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import personal.yeongyulgori.post.domain.post.model.dto.PostRequestDto;
import personal.yeongyulgori.post.domain.post.model.dto.PostResponseDto;

public interface PostService {

    PostResponseDto registerPost(Long userId, PostRequestDto postRegisterDto);

    PostResponseDto getPost(Long id, Long writerId);

    Page<PostResponseDto> getAllUserPosts(Long userId, Pageable pageable);

    PostResponseDto updatePost(Long id, Long userId, PostRequestDto postRequestDto);

    void deletePost(Long id, Long userId);

    Page<PostResponseDto> getSearchedPosts(String keyword, Pageable pageable);

}
