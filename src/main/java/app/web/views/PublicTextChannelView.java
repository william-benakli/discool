package app.web.views;

import app.jpa_repo.*;
import app.model.chat.PublicTextChannel;
import app.model.courses.Course;
import app.model.users.Person;
import app.web.layout.Navbar;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@Route(value = "channels", layout = Navbar.class)
public class PublicTextChannelView extends TextChannelView implements HasUrlParameter<Long> {

    private final PublicTextChannelRepository publicTextChannelRepository;

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


    @Override
    public void createChatBar() {
        selectRep = new MessageResponsePopComponent();
        layoutMaster.getStyle().set("display", "block");
        if (((PublicTextChannel) textChannel).isMute()) {
            if (currentUser.getRole() == Person.Role.STUDENT) {
                final Paragraph p = new Paragraph("Ce channel est reservé aux professeurs, impossible d'envoyer un message");
                p.getStyle().set("size", "54px").set("font-weight", "bold");
                layoutMaster.add(p);
            } else {
                layoutMaster.add(selectRep);
            }
        } else {
            layoutMaster.add(selectRep);
        }

        Button settings = createButtonSettings();
        if (currentUser.getRole() != Person.Role.STUDENT) {
            messageInputBar.addComponentAsFirst(settings);
        }
        super.createChatBar();
    }

    private Button createButtonSettings() {
        Button settings = new Button(new Icon(VaadinIcon.COGS));
        settings.getStyle().set("color", ColorHTML.PURPLE.getColorHtml());
        Dialog d = createDialogSettings();
        settings.addClickListener(event -> {
            if (currentUser.getRole() != Person.Role.STUDENT)
                d.open();
        });
        return settings;
    }

    private Dialog createDialogSettings() {
        final H2 title = new H2("Modification du channel");
        final Dialog settingsDialog = new Dialog();
        final TextField name = new TextField();
        final Button valider = new Button("Valider");
        final Button clear = new Button("Supprimer tous les messages");

        final VerticalLayout layout = new VerticalLayout();
        final HorizontalLayout layoutButton = new HorizontalLayout();
        final HorizontalLayout layoutCheckBox = new HorizontalLayout();

        final Checkbox mute = new Checkbox("Les élèves ne peuvent pas écrire dans ce salon");
        final Checkbox visible = new Checkbox("Salon visible uniquement par les professeurs");
        final PublicTextChannel channel = getController().getTextChannel(textChannel.getId());
        layout.setAlignItems(Alignment.START);

        title.getStyle().set("color", ColorHTML.PURPLE.getColorHtml());
        name.setLabel("Nom du salon : ");
        name.getStyle().set("color", ColorHTML.PURPLE.getColorHtml());
        name.setValue(channel.getName());
        mute.setValue(channel.isMute());
        visible.setValue(channel.isPrivateTeacher());
        layoutButton.add(clear, valider);
        layoutCheckBox.add(mute, visible);
        layout.add(title, name, layoutCheckBox, layoutButton);
        valider.addClickListener(event -> {
            getController().updateTextChannel(channel, name.getValue(), mute.getValue(), visible.getValue());
            settingsDialog.close();
            UI.getCurrent().getPage().reload();
            Notification.show("Vos changements ont bien été sauvegardés.");
        });

        clear.addClickListener(event -> {
            chatController.clearMessageChat();
            settingsDialog.close();
            UI.getCurrent().getPage().reload();
            Notification.show("Tous les messages du salon ont été supprimés.");
        });
        settingsDialog.add(layout);
        return settingsDialog;
    }
}
