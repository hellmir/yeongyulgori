package personal.yeongyulgori.post.domain.image.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import personal.yeongyulgori.post.domain.image.model.dto.ImageRequestDto;
import personal.yeongyulgori.post.domain.image.model.entity.Image;
import personal.yeongyulgori.post.domain.image.model.repository.ImageRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;

    public void saveImages(Long targetId, List<ImageRequestDto> imageRequestDtos) {

        List<Image> images = imageRequestDtos.stream()
                .map(imageRegistrationDto -> Image.from(targetId, imageRegistrationDto))
                .collect(Collectors.toList());

        imageRepository.saveAll(images);

    }

    /**
     * 클라이언트가 삭제한 이미지는 image 테이블에서 제거, 추가한 이미지는 image 테이블에 추가
     *
     * @param targetId          이미지 데이터 목록이 저장된 게시물, 댓글 또는 답글의 Primary Key
     * @param imageRequestDtos: 새로 갱신될 이미지 데이터 목록
     */
    public void updateImages(Long targetId, List<ImageRequestDto> imageRequestDtos) {

        List<Image> images = imageRepository.findAllByTargetIdOrderByIdAsc(targetId);

        List<Image> imagesToDelete = new ArrayList<>();
        List<Image> imagesToSave = new ArrayList<>();

        int idx = 0;

        for (ImageRequestDto imageRequestDto : imageRequestDtos) {

            boolean isSameImage = false;

            for (int i = idx; i < images.size(); i++) {

                if (images.get(i).getUrl().equals(imageRequestDto.getUrl())
                        && images.get(i).getPositionInContent() == imageRequestDto.getPositionInContent()) {

                    isSameImage = true;
                    idx = i + 1;
                    break;

                } else {
                    imagesToDelete.add(images.get(i));
                }

            }

            if (!isSameImage) {
                imagesToSave.add(Image.from(targetId, imageRequestDto));
            }

        }

        imageRepository.deleteAll(imagesToDelete);
        imageRepository.saveAll(imagesToSave);

    }

    public void deleteAllImages(Long targetId) {
        imageRepository.deleteAllByTargetId(targetId);
    }

}
