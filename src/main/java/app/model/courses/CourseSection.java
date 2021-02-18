package app.model.courses;

import lombok.Builder;
import lombok.Getter;

import javax.persistence.*;

@Builder
@Entity(name = "course_sections")
@Table(name = "course_sections")
public class CourseSection {

    @Getter
    @Id // to say this is the primary key in the database
    @GeneratedValue(strategy = GenerationType.IDENTITY) // to generate the id
    @Column(
            name = "id", // the name of the column in the database
            updatable = false // so that the value can't be updated
    )
    private Long id;

    @Getter
    @Column(
            name = "courseid",
            nullable = false
    )
    private Long courseId;

    @Getter
    @Column(
            name = "parentid",
            nullable = false
    )
    private Long parentId;

    @Getter
    @Column(
            name = "title",
            nullable = false
    )
    private String title;

    @Getter
    @Column(
            name = "content",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String content;
}
