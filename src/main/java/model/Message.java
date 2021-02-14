package model;

import lombok.Getter;

import javax.persistence.*;

/**
 * Abstract class that contains all the common fields between a message
 * in the public channels and the direct messages.
 */
public abstract class Message {

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
    private String sender;

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
}
