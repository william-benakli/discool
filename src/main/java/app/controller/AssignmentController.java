package app.controller;

import app.jpa_repo.AssignmentRepository;
import app.jpa_repo.CourseRepository;
import app.jpa_repo.PersonRepository;
import app.jpa_repo.StudentAssignmentsUploadsRepository;
import app.model.courses.StudentAssignmentUpload;

import java.util.Optional;


public class AssignmentController {
    private final PersonRepository personRepository;
    private final AssignmentRepository assignmentRepository;
    private final StudentAssignmentsUploadsRepository studentAssignmentsUploadsRepository;
    private final CourseRepository courseRepository;

    public AssignmentController(PersonRepository personRepository,
                                AssignmentRepository assignmentRepository,
                                StudentAssignmentsUploadsRepository studentAssignmentsUploadsRepository,
                                CourseRepository courseRepository) {
        this.personRepository = personRepository;
        this.assignmentRepository = assignmentRepository;
        this.studentAssignmentsUploadsRepository = studentAssignmentsUploadsRepository;
        this.courseRepository = courseRepository;
    }

    /**
     * Adds the record to the database if needed
     *
     * @param assignmentId The id of the assignment the file belongs in
     * @param courseId     The id of the course that the assignment belongs in
     * @param studentId    The id of the student who submitted the file
     */
    public void save(long assignmentId, long courseId, long studentId) {
        long date = System.currentTimeMillis();
        String uploadId = String.valueOf(assignmentId) + String.valueOf(studentId);

        Optional<StudentAssignmentUpload> upload = studentAssignmentsUploadsRepository.findById(Long.parseLong(uploadId));

        if (upload.isEmpty()) {
            StudentAssignmentUpload submissionToSave = StudentAssignmentUpload.builder()
                    .id(Long.parseLong(uploadId))
                    .assignmentId(assignmentId)
                    .courseId(courseId)
                    .studentId(studentId)
                    .grade(-1)
                    .dateUpload(date)
                    .build();
            studentAssignmentsUploadsRepository.save(submissionToSave);
        }

    }

    public StudentAssignmentUpload findStudentAssignmentSubmission(long assignmentId, long studentId) {
        return studentAssignmentsUploadsRepository.findByAssignmentIdAndStudentId(assignmentId, studentId);
    }
}
