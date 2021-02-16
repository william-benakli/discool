package jpa_repo;

import model.chat.PublicChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicCharMessageRepository extends JpaRepository<PublicChatMessage, Long> {
}
