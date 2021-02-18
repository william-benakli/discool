package app.jpa_repo;

import app.model.courses.CourseSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;

public interface CourseSectionRepository extends JpaRepository<CourseSection, Long> {

    // automatically creates the method to query the result
    // (= we never have to implement any of the methods in this interface)
    ArrayList<CourseSection> findAllSectionsByCourseId(long id);

}
