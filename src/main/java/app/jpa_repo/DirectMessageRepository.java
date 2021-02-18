package app.jpa_repo;

import app.model.chat.DirectMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long> {
}
