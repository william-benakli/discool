package app.jpa_repo;

import app.model.courses.StudentAssignmentUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

public interface StudentAssignmentsUploadsRepository extends JpaRepository<StudentAssignmentUpload, Long> {

    StudentAssignmentUpload findByAssignmentIdAndStudentId(long assignmentId, long studentId);

    ArrayList<StudentAssignmentUpload> findAllByAssignmentId(long assignmentId);

    @Modifying
    @Transactional
    @Query(value =" DELETE FROM students_assignments_uploads WHERE courseid = :idparam ",nativeQuery = true)
    void deleteAllByCourseId(@Param("idparam") long id);

}
