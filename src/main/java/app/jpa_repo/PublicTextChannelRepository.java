package app.jpa_repo;

import app.model.chat.PublicTextChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

public interface PublicTextChannelRepository extends JpaRepository<PublicTextChannel, Long> {
    ArrayList<PublicTextChannel> findAllByCourseIdOrderByIdDesc(long courseId);

    PublicTextChannel findFirstByCourseIdOrderByIdDesc(long courseId);

    PublicTextChannel findTextChannelById(long courseId);

    @Modifying
    @Transactional
    @Query(value =" DELETE FROM channels WHERE courseid = :idparam ",nativeQuery = true)
    void deleteAllByCourseId(@Param("idparam") long id);

}
