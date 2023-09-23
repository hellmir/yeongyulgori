package personal.yeongyulgori.post.domain.image.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import personal.yeongyulgori.post.domain.image.model.dto.ImageRequestDto;
import personal.yeongyulgori.post.domain.image.model.entity.Image;
import personal.yeongyulgori.post.domain.image.model.repository.ImageRepository;
import personal.yeongyulgori.post.domain.post.model.entity.Post;
import personal.yeongyulgori.post.domain.post.model.repository.PostRepository;
import personal.yeongyulgori.post.domain.post.service.PostService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static personal.yeongyulgori.post.domain.image.model.constant.TargetType.POST;
import static testutil.TestObjectFactory.*;

@ActiveProfiles("test")
@SpringBootTest
class ImageServiceTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ImageService imageService;

    @Autowired
    private ImageRepository imageRepository;

    @AfterEach
    void tearDown() {
        postRepository.deleteAll();
        imageRepository.deleteAll();
    }

    @DisplayName("전송된 게시물 이미지의 목록을 저장할 수 있다.")
    @Test
    void saveImages() {

        // given
        Post post = postRepository.save(createPostWithoutIdAndImages(3L, "abc"));
        List<ImageRequestDto> imageRequestDtos = createImageRequestDtos(POST);

        // when
        imageService.saveImages(post.getId(), imageRequestDtos);

        List<Image> images = imageRepository.findAll();

        // then
        assertThat(images).hasSize(3)
                .extracting("targetType", "name", "url", "positionInContent")
                .containsExactlyInAnyOrder(
                        tuple(POST, "test_image1",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample1.jpg", 10),
                        tuple(POST, "test_image2",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample2.jpg", 15),
                        tuple(POST, "test_image3",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample3.jpg", 18)
                );

    }

    @DisplayName("삭제하지 않은 이미지들을 유지하면서, 기존 이미지 목록을 새로운 이미지 목록으로 대체할 수 있다.")
    @Test
    void updateImages() {

        // given
        Post post = postRepository.save(createPostWithoutIdAndImages(3L, "abc"));

        createImageRequestDto(POST, "test_image1",
                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample1.jpg", 10);

        createImageRequestDto(POST, "test_image2",
                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample2.jpg", 12);

        createImageRequestDto(POST, "test_image3",
                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample3.jpg", 15);

        createImageRequestDto(POST, "test_image4",
                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample4.jpg", 18);

        createImageRequestDto(POST, "test_image5",
                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample5.jpg", 23);

        List<ImageRequestDto> imageRequestDtos = createImageRequestDtos(POST);

        imageService.saveImages(post.getId(), imageRequestDtos);

        ImageRequestDto newImageRequestDto1 = createImageRequestDto
                (POST, "test_image2",
                        "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample2.jpg", 12);

        ImageRequestDto newImageRequestDto2 = createImageRequestDto
                (POST, "test_image4",
                        "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample4.jpg", 18);

        ImageRequestDto newImageRequestDto3 = createImageRequestDto
                (POST, "test_image6",
                        "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample6.jpg", 20);

        ImageRequestDto newImageRequestDto4 = createImageRequestDto
                (POST, "test_image7",
                        "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample7.jpg", 25);

        List<ImageRequestDto> newImageRequestDtos
                = List.of(newImageRequestDto1, newImageRequestDto2, newImageRequestDto3, newImageRequestDto4);

        // when
        imageService.updateImages(post.getId(), newImageRequestDtos);

        List<Image> images = imageRepository.findAll();

        // then
        assertThat(images).hasSize(4)
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

    @DisplayName("첫 번째 이미지를 포함한 삭제하지 않은 이미지들을 유지하면서, 기존 이미지 목록을 새로운 이미지 목록으로 대체할 수 있다.")
    @Test
    void updateImagesWithFirstEdgeCase() {

        // given
        Post post = postRepository.save(createPostWithoutIdAndImages(3L, "abc"));

        createImageRequestDto(POST, "test_image1",
                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample1.jpg", 10);

        createImageRequestDto(POST, "test_image2",
                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample2.jpg", 12);

        createImageRequestDto(POST, "test_image3",
                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample3.jpg", 15);

        createImageRequestDto(POST, "test_image4",
                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample4.jpg", 18);

        createImageRequestDto(POST, "test_image5",
                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample5.jpg", 23);

        List<ImageRequestDto> imageRequestDtos = createImageRequestDtos(POST);

        imageService.saveImages(post.getId(), imageRequestDtos);

        ImageRequestDto newImageRequestDto1 = createImageRequestDto
                (POST, "test_image1",
                        "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample1.jpg", 10);

        ImageRequestDto newImageRequestDto2 = createImageRequestDto
                (POST, "test_image4",
                        "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample4.jpg", 18);

        ImageRequestDto newImageRequestDto3 = createImageRequestDto
                (POST, "test_image6",
                        "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample6.jpg", 20);

        ImageRequestDto newImageRequestDto4 = createImageRequestDto
                (POST, "test_image7",
                        "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample7.jpg", 25);

        List<ImageRequestDto> newImageRequestDtos
                = List.of(newImageRequestDto1, newImageRequestDto2, newImageRequestDto3, newImageRequestDto4);

        // when
        imageService.updateImages(post.getId(), newImageRequestDtos);

        List<Image> images = imageRepository.findAll();

        // then
        assertThat(images).hasSize(4)
                .extracting("targetType", "name", "url", "positionInContent")
                .containsExactlyInAnyOrder(
                        tuple(POST, "test_image1",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample1.jpg", 10),
                        tuple(POST, "test_image4",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample4.jpg", 18),
                        tuple(POST, "test_image6",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample6.jpg", 20),
                        tuple(POST, "test_image7",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample7.jpg", 25)
                );

    }

    @DisplayName("마지막 이미지를 포함한 삭제하지 않은 이미지들을 유지하면서, 기존 이미지 목록을 새로운 이미지 목록으로 대체할 수 있다.")
    @Test
    void updateImagesWithLastEdgeCase() {

        // given
        Post post = postRepository.save(createPostWithoutIdAndImages(3L, "abc"));

        createImageRequestDto(POST, "test_image1",
                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample1.jpg", 10);

        createImageRequestDto(POST, "test_image2",
                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample2.jpg", 12);

        createImageRequestDto(POST, "test_image3",
                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample3.jpg", 15);

        createImageRequestDto(POST, "test_image5",
                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample4.jpg", 18);

        createImageRequestDto(POST, "test_image5",
                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample5.jpg", 23);

        List<ImageRequestDto> imageRequestDtos = createImageRequestDtos(POST);

        imageService.saveImages(post.getId(), imageRequestDtos);

        ImageRequestDto newImageRequestDto1 = createImageRequestDto
                (POST, "test_image2",
                        "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample2.jpg", 12);

        ImageRequestDto newImageRequestDto2 = createImageRequestDto
                (POST, "test_image5",
                        "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample5.jpg", 23);

        ImageRequestDto newImageRequestDto3 = createImageRequestDto
                (POST, "test_image6",
                        "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample6.jpg", 20);

        ImageRequestDto newImageRequestDto4 = createImageRequestDto
                (POST, "test_image7",
                        "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample7.jpg", 25);

        List<ImageRequestDto> newImageRequestDtos
                = List.of(newImageRequestDto1, newImageRequestDto2, newImageRequestDto3, newImageRequestDto4);

        // when
        imageService.updateImages(post.getId(), newImageRequestDtos);

        List<Image> images = imageRepository.findAll();

        // then
        assertThat(images).hasSize(4)
                .extracting("targetType", "name", "url", "positionInContent")
                .containsExactlyInAnyOrder(
                        tuple(POST, "test_image2",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample2.jpg", 12),
                        tuple(POST, "test_image5",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample5.jpg", 23),
                        tuple(POST, "test_image6",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample6.jpg", 20),
                        tuple(POST, "test_image7",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample7.jpg", 25)
                );

    }

    @DisplayName("기존 이미지 목록에 일부 이미지들을 추가할 수 있다.")
    @Test
    void addImages() {

        // given
        Post post = postRepository.save(createPostWithoutIdAndImages(3L, "abc"));

        createImageRequestDto(POST, "test_image1",
                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample1.jpg", 10);

        createImageRequestDto(POST, "test_image2",
                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample2.jpg", 12);

        createImageRequestDto(POST, "test_image3",
                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample3.jpg", 15);

        createImageRequestDto(POST, "test_image5",
                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample4.jpg", 18);

        createImageRequestDto(POST, "test_image5",
                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample5.jpg", 23);

        List<ImageRequestDto> imageRequestDtos = createImageRequestDtos(POST);

        imageService.saveImages(post.getId(), imageRequestDtos);

        ImageRequestDto newImageRequestDto1 = createImageRequestDto
                (POST, "test_image6",
                        "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample6.jpg", 20);

        ImageRequestDto newImageRequestDto2 = createImageRequestDto
                (POST, "test_image7",
                        "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample7.jpg", 25);

        List<ImageRequestDto> newImageRequestDtos
                = List.of(newImageRequestDto1, newImageRequestDto2);

        // when
        imageService.updateImages(post.getId(), newImageRequestDtos);

        List<Image> images = imageRepository.findAll();

        // then
        assertThat(images).hasSize(2)
                .extracting("targetType", "name", "url", "positionInContent")
                .containsExactlyInAnyOrder(
                        tuple(POST, "test_image6",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample6.jpg", 20),
                        tuple(POST, "test_image7",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample7.jpg", 25)
                );

    }

    @DisplayName("기존 이미지 목록의 일부 이미지들을 삭제할 수 있다.")
    @Test
    void deleteImages() {

        // given
        Post post = postRepository.save(createPostWithoutIdAndImages(3L, "abc"));

        createImageRequestDto(POST, "test_image1",
                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample1.jpg", 10);

        createImageRequestDto(POST, "test_image2",
                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample2.jpg", 12);

        createImageRequestDto(POST, "test_image3",
                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample3.jpg", 15);

        createImageRequestDto(POST, "test_image5",
                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample4.jpg", 18);

        createImageRequestDto(POST, "test_image5",
                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample5.jpg", 23);

        List<ImageRequestDto> imageRequestDtos = createImageRequestDtos(POST);

        imageService.saveImages(post.getId(), imageRequestDtos);

        ImageRequestDto newImageRequestDto1 = createImageRequestDto
                (POST, "test_image2",
                        "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample2.jpg", 12);

        ImageRequestDto newImageRequestDto2 = createImageRequestDto
                (POST, "test_image5",
                        "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample5.jpg", 23);

        List<ImageRequestDto> newImageRequestDtos
                = List.of(newImageRequestDto1, newImageRequestDto2);

        // when
        imageService.updateImages(post.getId(), newImageRequestDtos);

        List<Image> images = imageRepository.findAll();

        // then
        assertThat(images).hasSize(2)
                .extracting("targetType", "name", "url", "positionInContent")
                .containsExactlyInAnyOrder(
                        tuple(POST, "test_image2",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample2.jpg", 12),
                        tuple(POST, "test_image5",
                                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample5.jpg", 23)
                );

    }

    @DisplayName("기존 이미지 목록의 모든 이미지를 삭제할 수 있다.")
    @Test
    @Transactional
    void deleteAllImages() {

        // given
        Post post = postRepository.save(createPostWithoutIdAndImages(3L, "abc"));

        createImageRequestDto(POST, "test_image1",
                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample1.jpg", 10);

        createImageRequestDto(POST, "test_image2",
                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample2.jpg", 12);

        createImageRequestDto(POST, "test_image3",
                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample3.jpg", 15);

        createImageRequestDto(POST, "test_image5",
                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample4.jpg", 18);

        createImageRequestDto(POST, "test_image5",
                "https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample5.jpg", 23);

        List<ImageRequestDto> imageRequestDtos = createImageRequestDtos(POST);

        imageService.saveImages(post.getId(), imageRequestDtos);

        // when
        imageService.deleteAllImages(post.getId());

        List<Image> images = imageRepository.findAll();

        // then
        assertThat(images).isEmpty();

    }

}