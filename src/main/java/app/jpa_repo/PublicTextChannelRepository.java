package app.jpa_repo;

import app.model.chat.PublicTextChannel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;

public interface PublicTextChannelRepository extends JpaRepository<PublicTextChannel, Long> {
    ArrayList<PublicTextChannel> findAllByCourseId(long courseId);

    PublicTextChannel findFirstByCourseIdOrderByIdDesc(long courseId);

    PublicTextChannel findTextChannelById(long courseId);

}
