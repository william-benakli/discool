package app.model.users;

import lombok.Builder;
import lombok.Getter;

import javax.persistence.*;

@Builder
@Entity(name = "group_members")
@Table(name = "group_members")
public class GroupMembers {
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
            name = "groupid",
            nullable = false
    )
    private Long groupId;

    @Getter
    @Column(
            name = "userid",
            nullable = false
    )
    private Long userId;

    @Getter
    @Column(
            name = "timeadded",
            nullable = false
    )
    private Long timeAdded;

}
