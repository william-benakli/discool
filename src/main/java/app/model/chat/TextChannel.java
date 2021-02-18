package app.model.chat;

import lombok.Builder;
import lombok.Getter;

import javax.persistence.*;

/**
 * This class represents a text channel. It is linked to the
 * "channels" table in the database.
 */
@Builder
@Entity(name = "channels")
@Table(name = "channels")
public class TextChannel {

    @Getter
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
    @Getter
    @Column(
            name = "courseid",
            nullable = false
    )
    private long courseId;

    /**
     * The name of the channel.
     */
    @Getter
    @Column(
            name = "name",
            nullable = false
    )
    private String name;

}
