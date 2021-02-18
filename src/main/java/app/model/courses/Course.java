package app.model.courses;

import lombok.Builder;
import lombok.Getter;

import javax.persistence.*;

@Builder
@Entity(name = "courses")
@Table(name = "courses")
public class Course {

    @Getter
    @Id // to say this is the primary key in the database
    @GeneratedValue(strategy = GenerationType.IDENTITY) // to generate the id
    @Column(
            name = "id", // the name of the column in the database
            updatable = false // so that the value can't be updated
    )
    private long id;

    @Getter
    @Column(
            name = "name",
            nullable = false
    )
    private String name;

    @Getter
    @Column(
            name = "teacherid",
            nullable = false
    )
    private long teacherId;
}
