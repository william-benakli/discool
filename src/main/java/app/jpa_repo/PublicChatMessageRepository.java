package app.jpa_repo;

import app.model.chat.PublicChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

public interface PublicChatMessageRepository extends JpaRepository<PublicChatMessage, Long> {

    ArrayList<PublicChatMessage> findAllByChannelid(long channelId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE posts set DELETED = 1 WHERE id = id")
    void updateDeletedById(@Param(value = "id") long id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE posts set MESSAGE = :msg WHERE ID = :idpost")
    void updateMessageById(@Param("idpost") long id, @Param("msg") String messageText);


    @Modifying
    @Transactional
    @Query(value = "UPDATE posts set DELETED = 1")
    void updateDeletedAll();

    @Modifying
    @Transactional
    @Query(value = "UPDATE posts SET DELETED = 1 WHERE channelid = :idchannel ORDER BY ID DESC LIMIT :limitation", nativeQuery = true)
    void updateDeleted(@Param("idchannel") long channel, @Param("limitation") int limit);

}
