package app.jpa_repo;

import app.model.chat.TextChannel;
import com.vaadin.flow.component.Component;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.ArrayList;

public interface TextChannelRepository extends JpaRepository<TextChannel, Long> {
    ArrayList<TextChannel> findAllByCourseId(long courseId);
    TextChannel findFirstByCourseIdOrderByIdDesc(long courseId);
    TextChannel findTextChannelById(long courseId);
}
