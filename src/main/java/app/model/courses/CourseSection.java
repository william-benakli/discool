package app.model.courses;

import lombok.Builder;
import lombok.Getter;

import javax.persistence.*;

@Getter
@Builder
@Entity(name = "course_sections")
@Table(name = "course_sections")
public class CourseSection {

    @Id // to say this is the primary key in the database
    @GeneratedValue(strategy = GenerationType.IDENTITY) // to generate the id
    @Column(
            name = "id", // the name of the column in the database
            updatable = false // so that the value can't be updated
    )
    private Long id;

    @Column(
            name = "courseid",
            nullable = false
    )
    private Long courseId;

    @Column(
            name = "parentid",
            nullable = false
    )
    private Long parentId;

    @Column(
            name = "title",
            nullable = false
    )
    private String title;


    @Column(
            name = "content",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String content;
}
