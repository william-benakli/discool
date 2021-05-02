package app.jpa_repo;

import app.model.courses.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.ArrayList;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    ArrayList<Assignment> findAllByCourseId(long courseId);
    Assignment findFirstByCourseIdOrderByIdDesc(long courseId);
    Assignment findAssignmentById(long courseId);
}