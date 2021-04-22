package app.model.users;

import com.vaadin.flow.component.html.Image;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.io.File;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "users")
@Table(name = "users")
public class Person {

    @Id // to say this is the primary key in the database
    @GeneratedValue(strategy = GenerationType.IDENTITY) // to generate the id
    @Column(
            name = "id", // the name of the column in the database
            updatable = false // so that the value can't be updated
    )

    private long id;

    @Column(
            nullable = false,
            name = "password",
            columnDefinition = "TEXT"
    )
    private String password;

    @Column(
            name = "username",
            nullable = false
    )
    private String username;

    @Column(
            name = "role",
            nullable = false
    )
    @NotNull
    private Role role;

    @Column(
            name = "firstname",
            nullable = false
    )
    @NotNull
    @NotEmpty
    private String firstName;

    @Column(
            name = "lastname",
            nullable = false
    )
    @NotNull
    @NotEmpty
    private String lastName;

    @Column(
            name = "email",
            nullable = false,
            unique = true
    )
    private String email;

    @Column(
            name = "description",
            columnDefinition = "TEXT"
    )
    private String description;

    @Column(name = "website")
    private String website;

    @Column(
            name = "firstlogin",
            nullable = false
    )
    private long firstLogin;

    @Column(
            name = "lastlogin",
            nullable = false
    )
    private long lastLogin;

    @Column(
            name = "timecreated",
            nullable = false
    )
    private long timeCreated;

    public enum Role {
        ADMIN, TEACHER, STUDENT;
        public static Role[] getRole(){
            return Role.class.getEnumConstants();
        }
    }

    public String getRoleAsString() {
        return role.toString();
    }

    //TODO: find a way to see if the user is logged in
    public boolean isConnected(){ return true; }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Person) && ((Person)o).getId() == this.id;
    }

    /**
     * Gets the user's profile picture, or the default one.
     * The pictures are stored in webapp/profile_pictures/id_of_the_user
     * @return an Image
     */
    public Image getProfilePicture() {
        String fileName = "profile_pictures/" + id + ".jpg";
        System.out.println(fileName);
        File file = new File("src/main/webapp/" + fileName);
        if (! file.exists()) {
            fileName = "profile_pictures/default.jpg";
        }
        System.out.println("final " + fileName);
        return new Image(fileName, "profile picture");
    }

}
