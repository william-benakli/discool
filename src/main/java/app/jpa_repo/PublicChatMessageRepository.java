package app.jpa_repo;

import app.model.chat.PublicChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicChatMessageRepository extends JpaRepository<PublicChatMessage, Long> {
}
