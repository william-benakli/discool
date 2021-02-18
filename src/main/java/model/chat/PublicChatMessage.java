package model.chat;

import lombok.Builder;
import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * This class is the data model for the posts in the public channels.
 * It is linked to the data contained in the "posts" table.
 */
@Builder
@Entity(name = "posts")
@Table(name = "posts")
public class PublicChatMessage extends Message {
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
