package app.model.courses;

import lombok.*;

import javax.persistence.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "courses")
@Table(name = "courses")
public class Course {

    @Id // to say this is the primary key in the database
    @GeneratedValue(strategy = GenerationType.IDENTITY) // to generate the id
    @Column(
            name = "id", // the name of the column in the database
            updatable = false // so that the value can't be updated
    )
    private long id;

    @Column(
            name = "name",
            nullable = false
    )
    private String name;

    @Column(
            name = "teacherid",
            nullable = false
    )
    private long teacherId;

    @Column(
            name = "pathicon",
            nullable = false
    )
    private String pathIcon;
}
