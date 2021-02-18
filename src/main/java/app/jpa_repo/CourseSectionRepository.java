package app.jpa_repo;

import app.model.courses.CourseSection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseSectionRepository extends JpaRepository<CourseSection, Long> {
}
