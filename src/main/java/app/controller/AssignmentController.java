package app.controller;

import app.jpa_repo.AssignmentRepository;
import app.jpa_repo.CourseRepository;
import app.jpa_repo.PersonRepository;
import app.jpa_repo.StudentAssignmentsUploadsRepository;
import app.model.courses.Assignment;
import app.model.courses.StudentAssignmentUpload;
import app.web.views.TeacherAssignmentView;

import java.util.ArrayList;


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
     *  @param assignmentId The id of the assignment the file belongs in
     * @param courseId     The id of the course that the assignment belongs in
     * @param studentId    The id of the student who submitted the file
     */
    public void saveStudentUploadIfNeeded(long assignmentId, long courseId, long studentId) {
        long date = System.currentTimeMillis();
        StudentAssignmentUpload upload = studentAssignmentsUploadsRepository.
                findByAssignmentIdAndStudentId(assignmentId, studentId);
        if (upload == null) {
            StudentAssignmentUpload submissionToSave = StudentAssignmentUpload.builder()
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

    public ArrayList<Assignment> getAssignmentsForCourse(long courseId) {
        return assignmentRepository.findAllByCourseId(courseId);
    }

    public ArrayList<StudentAssignmentUpload> getUploadsForAssignment(long assignmentId) {
        return studentAssignmentsUploadsRepository.findAllByAssignmentId(assignmentId);
    }

    public void saveGrading(TeacherAssignmentView.RowModel model) {
        studentAssignmentsUploadsRepository.save(model.getUpload());
    }

    public void saveGrading(long studentId, long assignmentId, long courseId, int grade, String teacherComments) {
        StudentAssignmentUpload upload = studentAssignmentsUploadsRepository.findByAssignmentIdAndStudentId(assignmentId, studentId);
        if (upload == null) {
            StudentAssignmentUpload toSave = StudentAssignmentUpload.builder()
                    .assignmentId(assignmentId)
                    .courseId(courseId)
                    .grade(grade)
                    .teacherComments(teacherComments)
                    .studentId(studentId)
                    .build();
            studentAssignmentsUploadsRepository.save(toSave);
        } else {
            upload.setTeacherComments(teacherComments);
            upload.setGrade(grade);
            studentAssignmentsUploadsRepository.save(upload);
        }
    }
}
