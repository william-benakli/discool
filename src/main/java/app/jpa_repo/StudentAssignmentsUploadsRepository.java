package app.jpa_repo;

import app.model.courses.StudentAssignmentUpload;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;

public interface StudentAssignmentsUploadsRepository extends JpaRepository<StudentAssignmentUpload, Long> {

    StudentAssignmentUpload findByAssignmentIdAndStudentId(long assignmentId, long studentId);

    ArrayList<StudentAssignmentUpload> findAllByAssignmentId(long assignmentId);

}
