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
    ArrayList<Course> findAll();

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO courses (teacherid , name, pathicon) VALUES (:teacherid ,:name , :pathicon)", nativeQuery = true)
    void createServer(@Param(value = "teacherid") Long teacherid, @Param(value = "name") String name, @Param(value = "pathicon") String pathicon);

}
