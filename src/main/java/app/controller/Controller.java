package app.controller;


import app.jpa_repo.*;
import app.model.chat.PublicChatMessage;
import app.model.chat.TextChannel;
import app.model.courses.Course;
import app.model.courses.CourseSection;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;

public class Controller {

    private final TextChannelRepository textChannelRepository;
    private final PublicChatMessageRepository publicChatMessageRepository;
    private final PersonRepository personRepository;
    private final CourseRepository courseRepository;
    private final CourseSectionRepository courseSectionRepository;

    public Controller(PersonRepository personRepository,
                      TextChannelRepository textChannelRepository,
                      PublicChatMessageRepository publicChatMessageRepository,
                      CourseRepository courseRepository,
                      CourseSectionRepository courseSectionRepository) {
        this.publicChatMessageRepository = publicChatMessageRepository;
        this.textChannelRepository = textChannelRepository;
        this.personRepository = personRepository;
        this.courseRepository = courseRepository;
        this.courseSectionRepository = courseSectionRepository;
    }

    public String getTitleCourse(long id) {
        Optional<Course> c = courseRepository.findById(id);
        if (c.isPresent()) {
            return c.get().getName();
        } else {
            return null;
        }
    }

    public void saveMessage(String message, long channelId, long parentId, long userId) {
        PublicChatMessage messageToSave = PublicChatMessage.builder()
                .message(message)
                .channelid(channelId)
                .parentId(parentId)
                .sender(userId)
                .timeCreated(000) // TODO add time of creation
                .deleted(false)
                .build();
        publicChatMessageRepository.save(messageToSave);
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

    public String getUsernameOfSender(PublicChatMessage publicChatMessage) {
        return personRepository.findById(publicChatMessage.getSender()).getUsername();
    }

    public ArrayList<PublicChatMessage> getChatMessagesForChannel(long channelId) {
        return publicChatMessageRepository.findAllByChannelid(channelId);
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

}
