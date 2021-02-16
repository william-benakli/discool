package model.courses;

import lombok.Getter;

import javax.persistence.*;

public class CourseSection {

    @Getter
    @Id // to say this is the primary key in the database
    @SequenceGenerator( // the generator for the id
            name = "idGenerator",
            sequenceName = "idGenerator",
            allocationSize = 1 // to increment the id by 1 each time
    )
    @GeneratedValue( // to generate the id
            strategy = GenerationType.SEQUENCE,
            generator = "idGenerator"
    )
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
