package app.model.courses;

import lombok.*;

import javax.persistence.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "assignments")
@Table(name = "assignments")
public class Assignment {

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
            name = "name",
            nullable = false
    )
    private String name;

    @Column(
            name = "description",
            columnDefinition = "TEXT"
    )
    private String description;

    @Column(
            name = "duedate",
            nullable = false
    )
    private long duedate;

    @Column(
            name = "cutoffdate"
    )
    private long cutoffdate;

    @Column(
            name = "allowlate",
            nullable = false
    )
    private short allowLate; // 0 for false, 1 for true

    @Column(
            name = "maxgrade"
    )
    private short maxGrade;

    @Column(
            name = "maxattempts",
            nullable = false
    )
    private short maxAttempts;

}
