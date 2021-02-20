package app.jpa_repo;

import app.model.chat.TextChannel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;

public interface TextChannelRepository extends JpaRepository<TextChannel, Long> {

    ArrayList<TextChannel> findAllByCourseId(long courseId);
}
