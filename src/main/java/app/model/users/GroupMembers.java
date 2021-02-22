package app.model.users;

import lombok.*;

import javax.persistence.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "group_members")
@Table(name = "group_members")
public class GroupMembers {
    @Id // to say this is the primary key in the database
    @GeneratedValue(strategy = GenerationType.IDENTITY) // to generate the id
    @Column(
            name = "id", // the name of the column in the database
            updatable = false // so that the value can't be updated
    )
    private long id;

    @Column(
            name = "groupid",
            nullable = false
    )
    private long groupId;

    @Column(
            name = "userid",
            nullable = false
    )
    private long userId;

    @Column(
            name = "timeadded",
            nullable = false
    )
    private long timeAdded;

}
