package app.controller;


import app.jpa_repo.*;
import app.model.chat.PublicChatMessage;
import app.model.chat.TextChannel;
import app.model.courses.Course;
import app.model.courses.MoodlePage;
import app.model.users.Group;
import app.model.users.GroupMembers;
import app.model.users.Person;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public class Controller {

    private final TextChannelRepository textChannelRepository;
    private final PublicChatMessageRepository publicChatMessageRepository;
    private final PersonRepository personRepository;
    private final CourseRepository courseRepository;
    private final MoodlePageRepository moodlePageRepository;
    private final GroupRepository groupRepository;
    private final GroupMembersRepository groupMembersRepository;

    public Controller(PersonRepository personRepository,
                      TextChannelRepository textChannelRepository,
                      PublicChatMessageRepository publicChatMessageRepository,
                      CourseRepository courseRepository,
                      MoodlePageRepository moodlePageRepository,
                      GroupRepository groupRepository,
                      GroupMembersRepository groupMembersRepository) {
        this.publicChatMessageRepository = publicChatMessageRepository;
        this.textChannelRepository = textChannelRepository;
        this.personRepository = personRepository;
        this.courseRepository = courseRepository;
        this.moodlePageRepository = moodlePageRepository;
        this.groupRepository = groupRepository;
        this.groupMembersRepository = groupMembersRepository;
    }

    public String getTitleCourse(long id) {
        Optional<Course> c = courseRepository.findById(id);
        return c.map(Course::getName).orElse(null);
    }

    public PublicChatMessage saveMessage(String message, long channelId, long parentId, long userId) {
        PublicChatMessage messageToSave = PublicChatMessage.builder()
                .message(message)
                .channelid(channelId)
                .parentId(parentId)
                .sender(userId)
                .timeCreated(System.currentTimeMillis())
                .deleted(false)
                .build();
        publicChatMessageRepository.save(messageToSave);
        return messageToSave;
    }

    public void changeMessage(PublicChatMessage publicChatMessage, String messageText) {
        publicChatMessageRepository.updateMessageById(publicChatMessage.getId(), messageText);
    }

    public void saveMessage(PublicChatMessage message) {
        publicChatMessageRepository.save(message);
    }

    public ArrayList<TextChannel> getAllChannelsForCourse(long courseId) {
        return textChannelRepository.findAllByCourseId(courseId);
    }

    public void deleteMessage(PublicChatMessage message) {
        publicChatMessageRepository.updateDeletedById(message.getId());
    }

    public void deletePage(MoodlePage section) {
        moodlePageRepository.delete(section);
    }

    public String getUsernameOfSender(PublicChatMessage publicChatMessage) {
        return personRepository.findById(publicChatMessage.getSender()).getUsername();
    }

    public ArrayList<PublicChatMessage> getChatMessagesForChannel(long channelId) {
        return publicChatMessageRepository.findAllByChannelid(channelId);
    }

    public ArrayList<Person> getAllUser() {
        return personRepository.findAll();
    }


    public void updateSection(MoodlePage section, String title, String content) {
        section.setTitle(title);
        section.setContent(content);
        moodlePageRepository.save(section);
    }

    public void clearMessageChat() {
        publicChatMessageRepository.updateDeletedAll();
    }

    public void clearMessageChat(int value, long channelid) {
        publicChatMessageRepository.updateDeleted(channelid, value);
    }

    public PublicChatMessage getMessageById(long id) {
        return publicChatMessageRepository.findById(id);
    }

    public ArrayList<Person> getAllStudentsForCourse(long courseId) {
        ArrayList<Group> groups = groupRepository.findAllByCourseId(courseId);
        ArrayList<GroupMembers> members = new ArrayList<>();
        groups.forEach(group -> members.addAll(groupMembersRepository.findByGroupId(group.getId())));
        Set<Person> students = new LinkedHashSet<>(); // Set doesn't allow duplicates
        members.forEach(m -> {
            Person p = personRepository.findById(m.getUserId());
            if (Person.Role.STUDENT.equals(p.getRole())) students.add(p);
        });
        return new ArrayList<>(students);
    }

    public ArrayList<Person> getAllUsersForCourse(long courseId) {
        ArrayList<Group> groups = groupRepository.findAllByCourseId(courseId);
        ArrayList<GroupMembers> members = new ArrayList<>();
        groups.forEach(group -> members.addAll(groupMembersRepository.findByGroupId(group.getId())));
        Set<Person> users = new LinkedHashSet<>(); // Set doesn't allow duplicates
        members.forEach(m -> users.add(personRepository.findById(m.getUserId())));
        return new ArrayList<>(users);
    }

    public void createChannel(String name, long courseId) {
        TextChannel toSave = TextChannel.builder().name(name).courseId(courseId).build();
        textChannelRepository.save(toSave);
    }

    public void createMoodlePage(String title, long courseId) {
        MoodlePage toSave = MoodlePage.builder().courseId(courseId).title(title).content("").build();
        moodlePageRepository.save(toSave);
    }

}
