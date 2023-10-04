package personal.yeongyulgori.post.testutil;

import personal.yeongyulgori.post.domain.image.model.constant.TargetType;
import personal.yeongyulgori.post.domain.image.model.dto.ImageRequestDto;
import personal.yeongyulgori.post.domain.image.model.entity.Image;
import personal.yeongyulgori.post.domain.post.model.dto.PostRequestDto;
import personal.yeongyulgori.post.domain.post.model.entity.Post;

import java.util.List;

public class TestObjectFactory {

    public static ImageRequestDto createImageRequestDto
            (TargetType targetType, String name, String url, int positionInContent) {

        return ImageRequestDto.builder()
                .targetType(targetType)
                .name(name)
                .url(url)
                .positionInContent(positionInContent)
                .build();

    }

    public static List<ImageRequestDto> createImageRequestDtos(TargetType targetType) {

        ImageRequestDto imageRequestDto1 = ImageRequestDto.builder()
                .targetType(targetType)
                .name("test_image1")
                .url("https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample1.jpg")
                .positionInContent(10)
                .build();

        ImageRequestDto imageRequestDto2 = ImageRequestDto.builder()
                .targetType(targetType)
                .name("test_image2")
                .url("https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample2.jpg")
                .positionInContent(15)
                .build();

        ImageRequestDto imageRequestDto3 = ImageRequestDto.builder()
                .targetType(targetType)
                .name("test_image3")
                .url("https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample3.jpg")
                .positionInContent(18)
                .build();

        return List.of(imageRequestDto1, imageRequestDto2, imageRequestDto3);

    }

    public static Image createImage
            (Long targetId, TargetType targetType, String name, String url, int positionInContent) {

        return Image.builder()
                .targetId(targetId)
                .targetType(targetType)
                .name(name)
                .url(url)
                .positionInContent(positionInContent)
                .build();

    }

    public static List<Image> createImages(TargetType targetType) {

        Image image1 = Image.builder()
                .targetType(targetType)
                .name("test_image1")
                .url("https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample1.jpg")
                .positionInContent(10)
                .build();

        Image image2 = Image.builder()
                .targetType(targetType)
                .name("test_image2")
                .url("https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample2.jpg")
                .positionInContent(15)
                .build();

        Image image3 = Image.builder()
                .targetType(targetType)
                .name("test_image3")
                .url("https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample3.jpg")
                .positionInContent(18)
                .build();

        return List.of(image1, image2, image3);

    }

    public static List<Image> createImages(Long targetId, TargetType targetType) {

        Image image1 = Image.builder()
                .targetId(targetId)
                .targetType(targetType)
                .name("test_image1")
                .url("https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample1.jpg")
                .positionInContent(10)
                .build();

        Image image2 = Image.builder()
                .targetId(targetId)
                .targetType(targetType)
                .name("test_image2")
                .url("https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample2.jpg")
                .positionInContent(15)
                .build();

        Image image3 = Image.builder()
                .targetId(targetId)
                .targetType(targetType)
                .name("test_image3")
                .url("https://test-images-bucket.s3.us-west-1.amazonaws.com/images/sample3.jpg")
                .positionInContent(18)
                .build();

        return List.of(image1, image2, image3);

    }

    public static PostRequestDto createPostRequestDto
            (String content, List<ImageRequestDto> imageRequestDtos) {

        return PostRequestDto.builder()
                .content(content)
                .imageRequestDtos(imageRequestDtos)
                .build();

    }

    public static PostRequestDto createPostRequestDto(String content) {

        return PostRequestDto.builder()
                .content(content)
                .build();

    }

    public static PostRequestDto createPostUpdateDto(String content) {

        return PostRequestDto.builder()
                .content(content)
                .build();

    }

    public static PostRequestDto createPostUpdateDto(String content, List<ImageRequestDto> imageRequestDtos) {

        return PostRequestDto.builder()
                .content(content)
                .imageRequestDtos(imageRequestDtos)
                .build();

    }

    public static Post createPost(Long id, Long userId, String content, List<Image> images) {

        return Post.builder()
                .id(id)
                .userId(userId)
                .content(content)
                .images(images)
                .imageCount(images.size())
                .build();

    }

    public static Post createPostWithoutId(Long userId, String content, List<Image> images) {

        return Post.builder()
                .userId(userId)
                .content(content)
                .images(images)
                .imageCount(images.size())
                .build();

    }

    public static Post createPostWithoutIdAndImages(Long userId, String content) {

        return Post.builder()
                .userId(userId)
                .content(content)
                .build();

    }

}
