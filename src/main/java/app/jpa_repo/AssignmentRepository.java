package app.jpa_repo;

import app.model.courses.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.ArrayList;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    ArrayList<Assignment> findAllByCourseId(long courseId);
    Assignment findFirstByCourseIdOrderByIdDesc(long courseId);
    Assignment findAssignmentById(long courseId);

    @Modifying
    @Transactional
    @Query(value =" DELETE FROM assignments WHERE courseid = :idparam ",nativeQuery = true)
    void deleteAllByCourseId(@Param("idparam") long id);
}