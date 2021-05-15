package app.controller;

import app.jpa_repo.*;
import app.model.chat.*;
import app.model.users.Person;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Optional;

public class ChatController {
    private final PersonRepository personRepository;
    private final PublicChatMessageRepository publicChatMessageRepository;
    private final PublicTextChannelRepository publicTextChannelRepository;
    private final PrivateChatMessageRepository privateChatMessageRepository;
    private final PrivateTextChannelRepository privateTextChannelRepository;

    public ChatController(@Autowired PersonRepository personRepository,
                          @Autowired PublicTextChannelRepository publicTextChannelRepository,
                          @Autowired PublicChatMessageRepository publicChatMessageRepository,
                          @Autowired PrivateTextChannelRepository privateTextChannelRepository,
                          @Autowired PrivateChatMessageRepository privateChatMessageRepository) {
        this.personRepository = personRepository;
        this.privateChatMessageRepository = privateChatMessageRepository;
        this.privateTextChannelRepository = privateTextChannelRepository;
        this.publicChatMessageRepository = publicChatMessageRepository;
        this.publicTextChannelRepository = publicTextChannelRepository;
    }

    /**
     * @param id        the id of the message to find
     * @param isPrivate true if the message is from a private text channel, false otherwise
     * @return the corresponding message
     */
    public ChatMessage getMessageById(long id, boolean isPrivate) {
        if (isPrivate) {
            return privateChatMessageRepository.findById(id);
        } else {
            return publicChatMessageRepository.findById(id);
        }
    }

    /**
     * Saves the message to the database
     *
     * @param message   the content of the message
     * @param channelId the id of the channel
     * @param parentId  the id of the parent message (can be null)
     * @param userId    the id of the sender
     * @param isPrivate true if the message is from a private text channel, false otherwise
     * @return the message that was saved
     */
    public ChatMessage saveMessage(String message, long channelId, long parentId,
                                   long userId, boolean isPrivate, int type) {
        if (isPrivate) return savePrivateMessage(message, channelId, parentId, userId, type);
        else return savePublicMessage(message, channelId, parentId, userId, type);
    }

    private PrivateChatMessage savePrivateMessage(String message, long channelId, long parentId, long userId, int type) {
        PrivateChatMessage messageToSave = PrivateChatMessage.builder()
                .message(message)
                .channelid(channelId)
                .parentId(parentId)
                .sender(userId)
                .timeCreated(System.currentTimeMillis())
                .deleted(false)
                .type(type)
                .build();
        privateChatMessageRepository.save(messageToSave);
        return messageToSave;
    }

    private PublicChatMessage savePublicMessage(String message, long channelId, long parentId, long userId, int type) {
        PublicChatMessage messageToSave = PublicChatMessage.builder()
                .message(message)
                .channelid(channelId)
                .parentId(parentId)
                .sender(userId)
                .timeCreated(System.currentTimeMillis())
                .deleted(false)
                .type(type)
                .build();
        publicChatMessageRepository.save(messageToSave);
        return messageToSave;
    }

    /**
     * @param message the message to reload
     * @return the updated message from the database
     */
    public ChatMessage findMessageById(ChatMessage message) {
        if (message instanceof PrivateChatMessage) {
            return privateChatMessageRepository.findById(message.getId());
        } else {
            return publicChatMessageRepository.findById(message.getId());
        }

    }

    /**
     * @param textChannel the text channel to find the messages for
     * @return an array list of all the messages in the channel
     */
    public ArrayList<ChatMessage> getAllChatMessagesForChannel(TextChannel textChannel) {
        if (textChannel instanceof PrivateTextChannel) {
            return privateChatMessageRepository.findAllByChannelid(textChannel.getId());
        } else {
            return publicChatMessageRepository.findAllByChannelid(textChannel.getId());
        }
    }

    /**
     * Deletes the message from the database
     *
     * @param message the message to delete
     */
    public void deleteMessage(ChatMessage message) {
        if (message instanceof PrivateChatMessage) {
            privateChatMessageRepository.updateDeletedById(message.getId());
        } else {
            publicChatMessageRepository.deletePublicChatMessageById(message.getId());
        }
    }

    /**
     * Changes the content of a message
     *
     * @param chatMessage the message to modify
     * @param messageText the new content of the message
     */
    public void changeMessage(ChatMessage chatMessage, String messageText) {
        if (chatMessage instanceof PrivateChatMessage) {
            privateChatMessageRepository.updateMessageById(chatMessage.getId(), messageText);
        } else {
            publicChatMessageRepository.updateMessageById(chatMessage.getId(), messageText);
        }
    }

    /**
     * @param chatMessage the message
     * @return the id of the sender
     */
    public String getUsernameOfSender(ChatMessage chatMessage) {
        return personRepository.findById(chatMessage.getSender()).getUsername();
    }

    /**
     * Finds all the private channels the user is in. They can be either as "userA" or "userB"
     *
     * @param userId the id of the user
     * @return all of the private text channels the user is in
     */
    public ArrayList<PrivateTextChannel> findAllPrivateChannelsWithUser(long userId) {
        ArrayList<PrivateTextChannel> channels = privateTextChannelRepository.findAllByUserA(userId);
        channels.addAll(privateTextChannelRepository.findAllByUserB(userId));
        return channels;
    }

    /**
     * @param userId the id of the user
     * @return true if the user has unread messages, false otherwise
     */
    public boolean hasUnreadMessages(long userId) {
        ArrayList<PrivateTextChannel> channels = privateTextChannelRepository.findAllByUserA(userId);
        for (PrivateTextChannel channel : channels) {
            if (!channel.isUser1Read()) return true;
        }
        channels = privateTextChannelRepository.findAllByUserB(userId);
        for (PrivateTextChannel channel : channels) {
            if (!channel.isUser2Read()) return true;
        }
        return false;
    }

    /**
     * Creates a new private channel between the 2 users
     *
     * @param currentUserId the id of the person creating the channel
     * @param type          "nom" or "pseudo"
     * @param otherPerson   the name or the username of the second person in the channel
     * @return -1 if smth went wrong,
     * the id of the created channel otherwise
     */
    public long createNewPrivateChannel(long currentUserId, String type, String otherPerson) {
        if (otherPerson.equals("")) return -1;
        // get the other person in the chat
        Person p = findUserByNameOrUsername(type, otherPerson);
        if (p == null) return -1; // the other user doesn't exist
        if (p.getId() == currentUserId) return -1; // can't talk to yourself directly

        // if a channel between the users already exists, return its id
        Optional<PrivateTextChannel> a = privateTextChannelRepository.findByUserAAndUserB(p.getId(), currentUserId);
        Optional<PrivateTextChannel> b = privateTextChannelRepository.findByUserAAndUserB(currentUserId, p.getId());
        if (a.isPresent()) {
            return a.get().getId();
        } else if (b.isPresent()) {
            return b.get().getId();
        }
        // create and save the channel
        PrivateTextChannel toSave = PrivateTextChannel.builder()
                .userA(currentUserId)
                .userB(p.getId())
                .user1Read(true)
                .user2Read(true)
                .build();
        privateTextChannelRepository.save(toSave);
        return toSave.getId();
    }

    private Person findUserByNameOrUsername(String type, String value) {
        if (type.equals("nom")) {
            String[] splitName = value.split(",");
            String firstName = splitName[0].trim();
            String lastName = splitName[1].trim();
            return personRepository.findByFirstNameAndLastName(firstName, lastName);
        } else {
            return personRepository.findByUsername(value);
        }
    }

    //----------------- The methods for the chat commands
    public void clearMessageChat() {
        publicChatMessageRepository.updateDeletedAll();
    }

    public void clearMessageChat(int value, long channelid) {
        publicChatMessageRepository.updateDeleted(channelid, value);
    }

    public PrivateTextChannel lastDm(long id) {
        return privateTextChannelRepository.findFirstByUserAOrderByIdDesc(id);
    }
}
