package app.jpa_repo;

import app.model.courses.MoodlePage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

public interface MoodlePageRepository extends JpaRepository<MoodlePage, Long> {
    ArrayList<MoodlePage> findAllByCourseId(long id);
    MoodlePage findFirstByCourseIdOrderByIdDesc(long courseId);
    MoodlePage findMoodlePageById(long courseId);

    @Modifying
    @Transactional
    @Query(value =" DELETE FROM moodle_pages WHERE courseid = :idparam ",nativeQuery = true)
    void deleteAllByCourseId(@Param("idparam") long id);

    MoodlePage findByCourseIdAndHomePage(long id, Boolean homePage);

}
