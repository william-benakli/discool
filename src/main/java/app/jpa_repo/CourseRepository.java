package app.jpa_repo;

import app.model.courses.Course;
import app.model.users.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

public interface CourseRepository extends JpaRepository<Course, Long> {

    //public void deleteCourseById(long id);

    @Modifying
    @Transactional
    @Query(value =" DELETE FROM courses WHERE id = :idparam ",nativeQuery = true)
    void deleteById(@Param("idparam") long id);
}
