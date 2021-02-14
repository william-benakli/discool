package model;

import lombok.Builder;
import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * This class represents a direct/private message between two users.
 * It is linked to the "directMessages" table in the database.
 */
@Builder
@Entity(name = "directMessages")
@Table(name = "directMessages")
public class DirectMessage {

    private final MessageType type = MessageType.DM;

    /**
     * The user sending the message.
     */
    @Getter
    @Column(
            name = "useridfrom",
            nullable = false
    )
    private String sender;

    /**
     * The user receiving the message
     */
    @Getter
    @Column(
            name = "useridto",
            nullable = false
    )
    private String addresse;

    /**
     * The subject of the message
     */
    @Getter
    @Column(name = "subject")
    private String subject;
}
