package app.jpa_repo;

import app.model.chat.DirectMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long> {

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM direct_messages WHERE useridfrom = :idparam OR useridto = :idparam", nativeQuery = true)
    void deleteByUserId(@Param("idparam") long id);

}
