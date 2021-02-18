package app.model.users;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "groups")
@Table(name = "groups")
public class Group {

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
            name = "courseid",
            nullable = false
    )
    private long courseId;

    @Getter
    @Column(
            name = "name",
            nullable = false
    )
    private String name;

    @Getter
    @Column(
            name = "description",
            columnDefinition = "TEXT"
    )
    private String description;

    @Getter
    @Column(name = "enrolmentkey")
    private String enrolmnentKey;
}
