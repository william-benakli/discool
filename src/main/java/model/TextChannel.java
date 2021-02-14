package model;

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

    /**
     * The id of the course this channel is in.
     */
    @Getter
    @Column(
            name = "courseid",
            nullable = false
    )
    private Long courseId;

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
