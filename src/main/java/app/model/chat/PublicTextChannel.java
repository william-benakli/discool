package app.model.chat;

import lombok.*;

import javax.persistence.*;

/**
 * This class represents a text channel. It is linked to the
 * "channels" table in the database.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "channels")
@Table(name = "channels")
public class PublicTextChannel implements TextChannel {

    @Id // to say this is the primary key in the database
    @GeneratedValue(strategy = GenerationType.IDENTITY) // to generate the id

    @Column(
            name = "id", // the name of the column in the database
            updatable = false // so that the value can't be updated
    )
    private long id;

    /**
     * The id of the course this channel is in.
     */
    @Column(
            name = "courseid",
            nullable = false
    )
    private long courseId;

    /**
     * The name of the channel.
     */
    @Column(
            name = "name",
            nullable = false
    )
    private String name;

}
