package app.jpa_repo;

import app.model.chat.ChatMessage;
import app.model.chat.PrivateChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

public interface PrivateChatMessageRepository extends JpaRepository<PrivateChatMessage, Long> {
    PrivateChatMessage findById(long id);

    ArrayList<ChatMessage> findAllByChannelid(long channelId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM direct_messages WHERE useridfrom = :idparam OR useridto = :idparam", nativeQuery = true)
    void deleteByUserId(@Param("idparam") long id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE posts set DELETED = 1 WHERE id = :id")
    void updateDeletedById(@Param(value = "id") long id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE posts set MESSAGE = :msg WHERE ID = :idpost")
    void updateMessageById(@Param("idpost") long id, @Param("msg") String messageText);


}
