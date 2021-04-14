package app.model.courses;

import app.jpa_repo.PersonRepository;
import app.model.users.Person;
import lombok.*;

import javax.persistence.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "students_assignments_uploads")
@Table(name = "students_assignments_uploads")
public class StudentAssignmentUpload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // to generate the id
    @Column(
            name = "id",
            updatable = false
    )
    private long id;

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
