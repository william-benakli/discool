package app.jpa_repo;

import app.model.courses.CourseSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

public interface CourseSectionRepository extends JpaRepository<CourseSection, Long> {

    ArrayList<CourseSection> findAllSectionsByCourseId(long id);

    CourseSection findByParentId(long id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE course_sections SET parentId = :new_id WHERE parentId = :old_id")
    void updateParentId(@Param(value = "old_id") Long old_id, @Param(value = "new_id") Long new_id);

}
