package app.model.chat;

public interface ChatMessage {
    long getId();

    long getSender();

    long getParentId();

    boolean isDeleted();

    String getMessage();

    long getTimeCreated();

    int getType();
}
