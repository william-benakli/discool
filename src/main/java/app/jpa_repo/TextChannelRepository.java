package app.jpa_repo;

import app.model.chat.TextChannel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TextChannelRepository extends JpaRepository<TextChannel, Long> {

}
