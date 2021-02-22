package app.jpa_repo;

import app.model.chat.PublicChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

public interface PublicChatMessageRepository extends JpaRepository<PublicChatMessage, Long> {

    ArrayList<PublicChatMessage> findAllByChannelidAndDeletedFalse(long channelId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE posts set DELETED = 1 WHERE id = id")
    void updateDeletedById(@Param(value = "id") long id);


}
