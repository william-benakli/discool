package app.model.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * This class is the data model for the posts in the public channels.
 * It is linked to the data contained in the "posts" table.
 */
@Builder
@AllArgsConstructor()
@NoArgsConstructor()
@Entity(name = "posts")
@Table(name = "posts")
public class PublicChatMessage {
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
            name = "message",
            nullable = false, // can't be null
            columnDefinition = "TEXT" // to precise the type of the field in the database
    )
    private String message;

    @Getter
    @Column(
            name = "userid",
            nullable = false
    )
    private Long sender;

    @Getter
    @Column(
            name = "timecreated",
            nullable = false
    )
    private Long timeCreated;

    /**
     * If this is a reply to another message, parentId is the id of that original message
     */
    @Getter
    @Column(name = "parentid")
    private Long parentId;

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
     * The id of the channel this post was sent to.
     */
    @Getter
    @Column(
            name = "channelid",
            nullable = false
    )
    private Long channelid;
}
