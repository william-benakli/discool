package app.model.chat;

import lombok.Builder;
import lombok.Getter;

import javax.persistence.*;

/**
 * This class represents a direct/private message between two users.
 * It is linked to the "directMessages" table in the database.
 */
@Builder
@Entity(name = "direct_messages")
@Table(name = "direct_messages")
public class DirectMessage {

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
            name = "message",
            nullable = false, // can't be null
            columnDefinition = "TEXT" // to precise the type of the field in the database
    )
    private String message;

    @Getter
    @Column(
            name = "timecreated",
            nullable = false
    )
    private long timeCreated;

    /**
     * If this is a reply to another message, parentId is the id of that original message
     */
    @Getter
    @Column(name = "parentid")
    private long parentId;

    /**
     * True if the message is deleted, false otherwise.
     * All messages are kept for monitoring purposes.
     */
    @Getter
    @Column(
            name = "deleted",
            columnDefinition = "bit",
            nullable = false
    )
    private boolean deleted;

    /**
     * The user sending the message.
     */
    @Getter
    @Column(
            name = "useridfrom",
            nullable = false
    )
    private long sender;

    /**
     * The user receiving the message
     */
    @Getter
    @Column(
            name = "useridto",
            nullable = false
    )
    private long addresse;

    /**
     * The subject of the message
     */
    @Getter
    @Column(name = "subject")
    private String subject;
}
