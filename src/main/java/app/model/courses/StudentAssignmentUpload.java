package app.model.courses;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "students_assignments_uploads")
@Table(name = "students_assignments_uploads")
public class StudentAssignmentUpload {

    @Id
    @Column(
            name = "id",
            updatable = false
    )
    private long id;
    // the id is the concatenation of 3 Strings : assignmentid + studentid + dateupload

    @Column(
            name = "assignmentid",
            updatable = false
    )
    private long assignmentId;

    @Column(
            name = "courseid",
            updatable = false
    )
    private long courseId;

    @Column(
            name = "studentid",
            updatable = false
    )
    private long studentId;

    @Column(
            name = "grade"
    )
    private int grade;

    @Column(
            name = "teachercomments",
            columnDefinition = "TEXT"
    )
    private String teacherComments;

    @Column(
            name = "dateupload",
            updatable = false
    )
    private long dateUpload;
}
