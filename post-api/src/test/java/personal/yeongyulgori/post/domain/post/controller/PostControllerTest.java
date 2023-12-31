package personal.yeongyulgori.post.domain.post.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import personal.yeongyulgori.post.domain.image.model.dto.ImageRequestDto;
import personal.yeongyulgori.post.domain.image.model.entity.Image;
import personal.yeongyulgori.post.domain.post.model.dto.PostRequestDto;
import personal.yeongyulgori.post.domain.post.model.dto.PostResponseDto;
import personal.yeongyulgori.post.domain.post.model.entity.Post;
import personal.yeongyulgori.post.domain.post.model.repository.PostRepository;
import personal.yeongyulgori.post.domain.post.service.PostService;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static personal.yeongyulgori.post.domain.image.model.constant.TargetType.POST;
import static personal.yeongyulgori.post.testutil.TestObjectFactory.*;

@ActiveProfiles("test")
@WebMvcTest(controllers = PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostService postService;

    @MockBean
    private PostRepository postRepository;

    @AfterEach
    void tearDown() {
        postRepository.deleteAllInBatch();
    }

    @DisplayName("사용자가 사용자 ID와 게시물 내용을 입력하면 게시물을 등록할 수 있다.")
    @Test
    void registerPost() throws Exception {

        // given
        List<ImageRequestDto> imageRequestDtos = createImageRequestDtos(POST);

        PostRequestDto postRequestDto = createPostRequestDto("abc", imageRequestDtos);

        List<Image> images = createImages(POST);

        Post post = createPostWithoutId(1L, "abc", images);

        PostResponseDto postResponseDto = PostResponseDto.from(post);

        when(postService.registerPost(eq(post.getUserId()), any(PostRequestDto.class)))
                .thenReturn(postResponseDto);

        // when, then
        mockMvc.perform(post("/posts/v1")
                        .param("userId", post.getUserId().toString())
                        .content(objectMapper.writeValueAsString(postRequestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated());

    }

    @DisplayName("게시물 ID를 입력하면 해당하는 게시물을 조회할 수 있다.")
    @Test
    void getPost() throws Exception {

        // given
        List<Image> images = createImages(POST);

        Post post = createPost(1L, 1L, "abc", images);

        PostResponseDto postResponseDto = PostResponseDto.from(post);

        when(postService.getPost(post.getId(), post.getUserId())).thenReturn(postResponseDto);

        // when, then
        mockMvc.perform(get("/posts/v1/{id}", post.getId())
                        .queryParam("userId", post.getUserId().toString()))
                .andDo(print())
                .andExpect(status().isOk());

    }

    @DisplayName("회원 ID를 입력하면 해당 회원의 모든 게시물을 조회할 수 있다.")
    @Test
    void getAllUserPosts() throws Exception {

        // given
        List<Image> images = createImages(POST);

        createPost(1L, 1L, "abc", images);

        createPost(3L, 2L, "abcd", images);

        Post post = createPost(4L, 1L, "abcde", images);

        Pageable pageable = Pageable.ofSize(10);

        List<PostResponseDto> postResponseDtos = new ArrayList<>();

        Page<PostResponseDto> userPage = new PageImpl<>(postResponseDtos, pageable, postResponseDtos.size());

        when(postService.getAllUserPosts(post.getUserId(), pageable)).thenReturn(userPage);

        // when, then
        mockMvc.perform(get("/posts/v1")
                        .queryParam("userId", post.getUserId().toString()))
                .andDo(print())
                .andExpect(status().isOk());

    }

    @DisplayName("일부 게시물 키워드를 입력하면 해당하는 게시물의 목록을 조회할 수 있다.")
    @Test
    void getSearchedPosts() throws Exception {

        // given
        String keyword = "bc";

        List<Image> images = createImages(POST);

        createPost(1L, 1L, "abc", images);

        createPost(3L, 2L, "abcd", images);

        createPost(4L, 3L, "abcde", images);

        Pageable pageable = Pageable.ofSize(10);

        List<PostResponseDto> postResponseDtos = new ArrayList<>();

        Page<PostResponseDto> userPage = new PageImpl<>(postResponseDtos, pageable, postResponseDtos.size());

        when(postService.getSearchedPosts(keyword, pageable)).thenReturn(userPage);

        // when, then
        mockMvc.perform(get("/posts/v1/search")
                        .queryParam("keyword", keyword))
                .andDo(print())
                .andExpect(status().isOk());

    }

    @DisplayName("게시물 ID와 사용자 ID, 수정할 내용을 입력하면 해당 게시물을 수정할 수 있다.")
    @Test
    void updatePost() throws Exception {

        // given
        List<Image> images = createImages(POST);

        Post post = createPost(1L, 1L, "abc", images);

        PostRequestDto postRequestDto = PostRequestDto.builder()
                .content("abcd")
                .build();

        PostResponseDto postResponseDto = PostResponseDto.from(post);

        when(postService
                .updatePost(eq(post.getId()), eq(post.getUserId()), any(PostRequestDto.class)))
                .thenReturn(postResponseDto);

        // when, then
        mockMvc.perform(put("/posts/v1/{id}", post.getId())
                        .param("userId", post.getUserId().toString())
                        .content(objectMapper.writeValueAsString(postRequestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

    }

    @DisplayName("사용자 ID와 게시물 ID를 입력하면 해당 게시물을 삭제할 수 있다.")
    @Test
    void deletePost() throws Exception {

        // given
        List<Image> images = createImages(POST);

        Post post = createPost(1L, 1L, "abc", images);

        // when, then

        mockMvc.perform(delete("/posts/v1/{id}", post.getId())
                        .param("userId", post.getUserId().toString()))
                .andDo(print())
                .andExpect(status().isNoContent());

    }

}
