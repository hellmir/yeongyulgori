package personal.yeongyulgori.post.domain.image.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import personal.yeongyulgori.post.domain.image.model.entity.Image;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {

    List<Image> findAllByTargetIdOrderByIdAsc(Long targetId);

    void deleteAllByTargetId(Long targetId);

}
