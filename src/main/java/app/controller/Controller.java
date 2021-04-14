package app.controller;


import app.jpa_repo.*;
import app.model.chat.PublicChatMessage;
import app.model.chat.TextChannel;
import app.model.courses.Course;
import app.model.courses.CourseSection;
import app.model.users.Group;
import app.model.users.GroupMembers;
import app.model.users.Person;

import java.util.*;

public class Controller {

    private final TextChannelRepository textChannelRepository;
    private final PublicChatMessageRepository publicChatMessageRepository;
    private final PersonRepository personRepository;
    private final CourseRepository courseRepository;
    private final CourseSectionRepository courseSectionRepository;
    private final GroupRepository groupRepository;
    private final GroupMembersRepository groupMembersRepository;

    public Controller(PersonRepository personRepository,
                      TextChannelRepository textChannelRepository,
                      PublicChatMessageRepository publicChatMessageRepository,
                      CourseRepository courseRepository,
                      CourseSectionRepository courseSectionRepository,
                      GroupRepository groupRepository,
                      GroupMembersRepository groupMembersRepository) {
        this.publicChatMessageRepository = publicChatMessageRepository;
        this.textChannelRepository = textChannelRepository;
        this.personRepository = personRepository;
        this.courseRepository = courseRepository;
        this.courseSectionRepository = courseSectionRepository;
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

    public void deleteSection(CourseSection section) {
        if (section.getParentId() == null) { // the section to delete is the first section
            courseSectionRepository.updateParentId(section.getId(), null);
        } else { // else just update the parentId
            courseSectionRepository.updateParentId(section.getId(), section.getParentId());
        }
        courseSectionRepository.delete(section);
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

    /**
     * @return all the course sections in the right order
     */
    public LinkedList<CourseSection> getAllSectionsInOrder(long courseId) {
        ArrayList<CourseSection> list = courseSectionRepository.findAllSectionsByCourseId(courseId); // get all the sections from the table
        list.forEach(courseSection -> courseSection.addParent(courseSectionRepository)); // add the parent for each element
        LinkedList<CourseSection> sortedList = CourseSection.sort(list); // sort the sections in the right order
        return sortedList;
    }

    public void updateSection(CourseSection section, String title, String content) {
        section.setTitle(title);
        section.setContent(content);
        courseSectionRepository.save(section);
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
        members.forEach(m -> students.add(personRepository.findById(m.getUserId())));
        return new ArrayList<>(students);
    }
}
