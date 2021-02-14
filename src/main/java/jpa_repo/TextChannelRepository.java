package jpa_repo;

import model.chat.TextChannel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TextChannelRepository extends JpaRepository<TextChannel, Long> {
}
