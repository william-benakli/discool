package app.jpa_repo;

import app.model.courses.MoodlePage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;

public interface MoodlePageRepository extends JpaRepository<MoodlePage, Long> {

    ArrayList<MoodlePage> findAllSectionsByCourseId(long id);

}
