package app.model.users;

import lombok.Builder;
import lombok.Getter;

import javax.persistence.*;

@Builder
@Entity(name = "users")
@Table(name = "users")
public class User {

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
            name = "username",
            nullable = false
    )
    private String username;
    @Getter
    @Column(
            name = "role",
            nullable = false
    )
    private Role role;
    @Getter
    @Column(
            name = "firstname",
            nullable = false
    )
    private String firstName;
    @Getter
    @Column(
            name = "lastname",
            nullable = false
    )
    private String lastName;
    @Getter
    @Column(
            name = "email",
            nullable = false,
            unique = true
    )
    private String email;
    @Getter
    @Column(
            name = "description",
            columnDefinition = "TEXT"
    )
    private String description;
    @Getter
    @Column(name = "website")
    private String website;
    @Getter
    @Column(
            name = "firstlogin",
            nullable = false
    )
    private Long firstLogin;
    @Getter
    @Column(
            name = "lastlogin",
            nullable = false
    )
    private Long lastLogin;
    @Getter
    @Column(
            name = "timecreated",
            nullable = false
    )
    private Long timeCreated;

    // TODO : make sure it converts to an int when saving to the database
    private enum Role {ADMIN, TEACHER, STUDENT}
}
