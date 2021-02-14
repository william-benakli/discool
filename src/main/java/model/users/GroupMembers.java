package model.users;

import lombok.Getter;

import javax.persistence.*;

public class GroupMembers {
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
