package app.jpa_repo;

import app.model.courses.Course;
import app.model.users.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;

public interface CourseRepository extends JpaRepository<Course, Long> {
    ArrayList<Course> findAll();


}
