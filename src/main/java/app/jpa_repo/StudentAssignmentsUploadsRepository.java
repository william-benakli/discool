package app.jpa_repo;

import app.model.courses.StudentAssignmentUpload;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentAssignmentsUploadsRepository extends JpaRepository<StudentAssignmentUpload, Long> {

    StudentAssignmentUpload findByAssignmentIdAndStudentId(long assignmentId, long studentId);

}
