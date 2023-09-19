package personal.yeongyulgori.post.domain.post.controller;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import personal.yeongyulgori.post.domain.post.model.dto.PostRequestDto;
import personal.yeongyulgori.post.domain.post.model.dto.PostResponseDto;
import personal.yeongyulgori.post.domain.post.service.PostService;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("posts/v1")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @ApiOperation(value = "게시물 등록", notes = "사용자 ID와 게시물 내용을 입력해 게시물을 등록할 수 있습니다.")
    @PostMapping
    public ResponseEntity<PostResponseDto> registerPost
            (@RequestParam @ApiParam(value = "사용자 ID", example = "1") Long userId,
             @Valid @RequestBody @ApiParam(value = "게시물 내용") PostRequestDto postRequestDto) {

        PostResponseDto postResponseDto = postService.registerPost(userId, postRequestDto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(postResponseDto.getId())
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(postResponseDto);

    }

    @ApiOperation(value = "게시물 조회", notes = "게시물 ID와 게시자 ID를 입력해 게시물을 조회할 수 있습니다.")
    @GetMapping("{id}")
    public ResponseEntity<PostResponseDto> getPost
            (@PathVariable @ApiParam(value = "게시물 ID", example = "1") Long id,
             @RequestParam(name = "userId") @ApiParam(value = "게시자 ID", example = "1") Long writerId) {

        PostResponseDto postResponseDto = postService.getPost(id, writerId);

        return ResponseEntity.status(HttpStatus.OK).body(postResponseDto);

    }

    @ApiOperation(value = "사용자의 게시물 목록 조회",
            notes = "회원 ID를 입력해 해당 회원의 게시물 목록을 조회할 수 있습니다.\n페이징 옵션을 선택할 수 있습니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "paged", value = "페이지네이션 사용 여부", dataTypeClass = Boolean.class,
                    paramType = "query", defaultValue = "true", example = "true"),
            @ApiImplicitParam(name = "unpaged", value = "페이지네이션 미사용 여부", dataTypeClass = Boolean.class,
                    paramType = "query", defaultValue = "false", example = "false"),
            @ApiImplicitParam(name = "offset", value = "목록 시작 위치", dataTypeClass = Integer.class,
                    paramType = "query", defaultValue = "0", example = "0"),
            @ApiImplicitParam(name = "page", value = "현재 페이지 번호", dataTypeClass = Integer.class,
                    paramType = "query", defaultValue = "0", example = "0"),
            @ApiImplicitParam(name = "pageNumber", value = "현재 페이지 번호", dataTypeClass = Integer.class,
                    paramType = "query", defaultValue = "10", example = "10"),
            @ApiImplicitParam(name = "size", value = "페이지 당 항목 수", dataTypeClass = Integer.class,
                    paramType = "query", defaultValue = "20", example = "20"),
            @ApiImplicitParam(name = "pageSize", value = "페이지 당 항목 수", dataTypeClass = Integer.class,
                    paramType = "query", defaultValue = "20", example = "20"),
            @ApiImplicitParam(name = "sort.sorted", value = "정렬 사용 여부", dataTypeClass = Boolean.class,
                    paramType = "query", defaultValue = "true", example = "true"),
            @ApiImplicitParam(name = "sort.unsorted", value = "정렬 미사용 여부", dataTypeClass = Boolean.class,
                    paramType = "query", defaultValue = "false", example = "false"),
            @ApiImplicitParam(name = "sort", value = "정렬 방식", dataTypeClass = String.class,
                    paramType = "query", defaultValue = "name,asc", example = "name,asc")
    })
    @GetMapping()
    public ResponseEntity<Page<PostResponseDto>> getAllUserPosts
            (@RequestParam(name = "userId") @ApiParam(value = "사용자 ID", example = "1") Long userId,
             Pageable pageable) {

        Page<PostResponseDto> postResponseDtos = postService.getAllUserPosts(userId, pageable);

        return ResponseEntity.status(HttpStatus.OK).body(postResponseDtos);

    }

    @ApiOperation(value = "특정 키워드를 포함하는 게시물 목록 조회",
            notes = "일부 게시물 키워드를 입력해 해당하는 게시물 목록을 조회할 수 있습니다.\n페이징 옵션을 선택할 수 있습니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "paged", value = "페이지네이션 사용 여부", dataTypeClass = Boolean.class,
                    paramType = "query", defaultValue = "true", example = "true"),
            @ApiImplicitParam(name = "unpaged", value = "페이지네이션 미사용 여부", dataTypeClass = Boolean.class,
                    paramType = "query", defaultValue = "false", example = "false"),
            @ApiImplicitParam(name = "offset", value = "목록 시작 위치", dataTypeClass = Integer.class,
                    paramType = "query", defaultValue = "0", example = "0"),
            @ApiImplicitParam(name = "page", value = "현재 페이지 번호", dataTypeClass = Integer.class,
                    paramType = "query", defaultValue = "0", example = "0"),
            @ApiImplicitParam(name = "pageNumber", value = "현재 페이지 번호", dataTypeClass = Integer.class,
                    paramType = "query", defaultValue = "10", example = "10"),
            @ApiImplicitParam(name = "size", value = "페이지 당 항목 수", dataTypeClass = Integer.class,
                    paramType = "query", defaultValue = "20", example = "20"),
            @ApiImplicitParam(name = "pageSize", value = "페이지 당 항목 수", dataTypeClass = Integer.class,
                    paramType = "query", defaultValue = "20", example = "20"),
            @ApiImplicitParam(name = "sort.sorted", value = "정렬 사용 여부", dataTypeClass = Boolean.class,
                    paramType = "query", defaultValue = "true", example = "true"),
            @ApiImplicitParam(name = "sort.unsorted", value = "정렬 미사용 여부", dataTypeClass = Boolean.class,
                    paramType = "query", defaultValue = "false", example = "false"),
            @ApiImplicitParam(name = "sort", value = "정렬 방식", dataTypeClass = String.class,
                    paramType = "query", defaultValue = "name,asc", example = "name,asc")
    })
    @GetMapping("search")
    public ResponseEntity<Page<PostResponseDto>> getSearchedPosts
            (@RequestParam(name = "keyword", defaultValue = "")
             @ApiParam(value = "게시물 키워드", example = "안녕") String keyword, Pageable pageable) {

        Page<PostResponseDto> postResponseDtos = postService.getSearchedPosts(keyword, pageable);

        return ResponseEntity.status(HttpStatus.OK).body(postResponseDtos);

    }

    @ApiOperation(value = "게시물 수정",
            notes = "게시물 ID, 사용자 ID, 게시물 내용을 입력해 게시물을 수정할 수 있습니다.")
    @PutMapping("{id}")
    public ResponseEntity<PostResponseDto> updatePost
            (@PathVariable @ApiParam(value = "게시물 ID", example = "1") Long id,
             @RequestParam @ApiParam(value = "사용자 ID", example = "1") Long userId,
             @Valid @RequestBody @ApiParam(value = "게시물 내용") PostRequestDto postRequestDto) {

        PostResponseDto postResponseDto = postService.updatePost(id, userId, postRequestDto);

        return ResponseEntity.status(HttpStatus.OK).body(postResponseDto);

    }

    @ApiOperation(value = "게시물 삭제", notes = "게시물 ID와 사용자 ID를 입력해 게시물을 삭제할 수 있습니다.")
    @DeleteMapping("{id}")
    public ResponseEntity<PostResponseDto> deletePost
            (@PathVariable @ApiParam(value = "게시물 ID", example = "1") Long id,
             @RequestParam @ApiParam(value = "사용자 ID", example = "1") Long userId) {

        postService.deletePost(id, userId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

}
