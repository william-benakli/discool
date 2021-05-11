package app.web.views;

import app.jpa_repo.*;
import app.model.chat.PublicTextChannel;
import app.model.courses.Course;
import app.web.layout.Navbar;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@Route(value = "channels", layout = Navbar.class)
public class PublicTextChannelView extends TextChannelView implements HasUrlParameter<Long> {

    private final PublicTextChannelRepository publicTextChannelRepository;
    private final PublicChatMessageRepository publicChatMessageRepository;

    public PublicTextChannelView(@Autowired PublicTextChannelRepository publicTextChannelRepository,
                                 @Autowired PublicChatMessageRepository publicChatMessageRepository,
                                 @Autowired PrivateChatMessageRepository privateChatMessageRepository,
                                 @Autowired PrivateTextChannelRepository privateTextChannelRepository,
                                 @Autowired PersonRepository personRepository,
                                 @Autowired AssignmentRepository assignmentRepository,
                                 @Autowired CourseRepository courseRepository,
                                 @Autowired StudentAssignmentsUploadsRepository studentAssignmentsUploadsRepository,
                                 @Autowired GroupRepository groupRepository,
                                 @Autowired GroupMembersRepository groupMembersRepository,
                                 @Autowired MoodlePageRepository moodlePageRepository) {
        super(publicTextChannelRepository, publicChatMessageRepository, privateTextChannelRepository,
              privateChatMessageRepository, personRepository,
              assignmentRepository, courseRepository, studentAssignmentsUploadsRepository,
              groupRepository, groupMembersRepository, moodlePageRepository);
        this.publicTextChannelRepository = publicTextChannelRepository;
        this.publicChatMessageRepository = publicChatMessageRepository;
    }


    @SneakyThrows // so that javac doesn't complain about dirty exception throwing
    @Override
    public void setParameter(BeforeEvent event, Long parameter) {
        Optional<PublicTextChannel> channel = publicTextChannelRepository.findById(parameter);
        if (channel.isPresent()) {
            textChannel = channel.get();
        } else {
            throw new Exception("There is no channel with this ID.");
            // TODO : take care of the exception
        }
        Optional<Course> c = courseRepository.findById(((PublicTextChannel) textChannel).getCourseId());
        setCourse(c.orElse(null));

        createSidebar(((PublicTextChannel) textChannel).getCourseId());
        createMembersBar(((PublicTextChannel) textChannel).getCourseId());
        createLayout(chatBar);
        createChatBar();
    }
}
