package fr.laurerunser.discool.model;

import lombok.Builder;
import lombok.Getter;

@Builder
public class MessageChat {
    @Getter
    private MessageType type ;

    @Getter
    private String content ;

    @Getter
    private String sender ;

    @Getter
    private String time ;
}
