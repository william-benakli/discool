package app.model.courses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity(name = "course_sections")
@Table(name = "course_sections")
public class CourseSection {


    @Id // to say this is the primary key in the database
    @GeneratedValue(strategy = GenerationType.IDENTITY) // to generate the id
    @Column(
            name = "id", // the name of the column in the database
            updatable = false // so that the value can't be updated
    )
    private long id;

    @Column(
            name = "courseid",
            nullable = false
    )
    private long courseId;

    @Column(
            name = "parentid",
            nullable = false
    )
    private long parentId;

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
