package app.jpa_repo;

import app.model.courses.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    ArrayList<Assignment> findAllByCourseId(long courseId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO assignments (courseid, name, description, duedate, cutoffdate, allowlate, maxgrade, maxattempts) VALUES (:courseid, :name, :description, :duedate, :cutoffdate, :allowlate, :maxgrade, :maxattempts)", nativeQuery = true)
    void createAssignement(
            @Param(value = "courseid") Long courseid
            , @Param(value = "name") String name
            , @Param(value = "description") String description
            , @Param(value = "duedate") int duedate
            , @Param(value = "cutoffdate") int cutoffdate
            , @Param(value = "allowlate") int allowlate
            , @Param(value = "maxgrade") int maxgrade
            , @Param(value = "maxattempts") int maxattempts);
}