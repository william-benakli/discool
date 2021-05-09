package app.model.chat;

import lombok.*;

import javax.persistence.*;

/**
 * This class represents a direct/private message between two users.
 * It is linked to the "directMessages" table in the database.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "direct_messages")
@Table(name = "direct_messages")
public class PrivateChatMessage implements ChatMessage {

    @Id // to say this is the primary key in the database
    @GeneratedValue(strategy = GenerationType.IDENTITY) // to generate the id
    @Column(
            name = "id", // the name of the column in the database
            updatable = false // so that the value can't be updated
    )
    private long id;

    @Column(
            name = "message",
            nullable = false, // can't be null
            columnDefinition = "TEXT" // to precise the type of the field in the database
    )
    private String message;

    @Column(
            name = "timecreated",
            nullable = false
    )
    private long timeCreated;

    /**
     * If this is a reply to another message, parentId is the id of that original message
     */
    @Column(name = "parentid")
    private long parentId;

    /**
     * True if the message is deleted, false otherwise.
     * All messages are kept for monitoring purposes.
     */
    @Column(
            name = "deleted",
            columnDefinition = "bit",
            nullable = false
    )
    private boolean deleted;

    /**
     * The user sending the message.
     */
    @Column(
            name = "senderid",
            nullable = false
    )
    private long sender;

    @Column(
            name = "channelid",
            nullable = false
    )
    private long channelid;

}
