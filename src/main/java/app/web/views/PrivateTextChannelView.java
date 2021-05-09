package app.web.views;

import app.jpa_repo.*;
import app.model.chat.PrivateTextChannel;
import app.model.users.Person;
import app.web.layout.Navbar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Optional;

@Route(value = "dms", layout = Navbar.class)
public class PrivateTextChannelView extends TextChannelView implements HasUrlParameter<Long> {
    private final PrivateTextChannelRepository privateTextChannelRepository;

    public PrivateTextChannelView(@Autowired PublicTextChannelRepository publicTextChannelRepository,
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
        this.privateTextChannelRepository = privateTextChannelRepository;
    }

    @SneakyThrows // so that javac doesn't complain about dirty exception throwing
    @Override
    public void setParameter(BeforeEvent event, Long parameter) {
        if (parameter != -1) {
            Optional<PrivateTextChannel> channel = privateTextChannelRepository.findById(parameter);
            if (channel.isPresent()) {
                textChannel = channel.get();
                createChatBar();
            }
        }
        createLeftSidebar();
        createLayout(chatBar);
    }


    /**
     * Create the side bar on the left that hold links to all of the private channels
     */
    private void createLeftSidebar() {
        sideBar = new FlexLayout();
        sideBar.addClassName("card");
        sideBar.addClassName("cardLeft");
        setCardStyle(sideBar, "20%", TextChannelView.ColorHTML.DARKGREY);

        VerticalLayout channelLayout = addChannelLinksToSideBar();
        Button addChannelButton = createAddChannelButton();
        sideBar.add(addChannelButton, channelLayout);
    }

    private VerticalLayout addChannelLinksToSideBar() {
        ArrayList<PrivateTextChannel> channels = chatController.findAllPrivateChannelsWithUser(currentUser.getId());
        VerticalLayout channelLayout = new VerticalLayout();
        for (PrivateTextChannel channel : channels) {
            channelLayout.add(createChannelButton(channel));
        }
        return channelLayout;
    }

    private Button createAddChannelButton() {
        Button button = new Button(new Icon(VaadinIcon.PLUS));
        button.addClickListener(event -> new AddChannelDialog());
        return button;
    }

    private RouterLink createChannelButton(PrivateTextChannel channel) {
        Person otherPerson = getOtherPerson(channel);
        RouterLink routerLink = new RouterLink("", PrivateTextChannelView.class, channel.getId());
        Button button = new Button(otherPerson.getUsername());
        styleButton(routerLink, button);
        return routerLink;
    }

    private Person getOtherPerson(PrivateTextChannel channel) {
        Person otherPerson;
        if (channel.getUserA() == currentUser.getId()) {
            otherPerson = personRepository.findById(channel.getUserB());
        } else {
            otherPerson = personRepository.findById(channel.getUserA());
        }
        return otherPerson;
    }

    private void styleButton(RouterLink link, Button button) {
        link.getElement().appendChild(button.getElement());
        link.getStyle()
                .set("pointer-event", "none")
                .set("padding-bottom", "2.5px")
                .set("background", "none")
                .set("margin-left", "3px");
        button.getStyle()
                .set("font-weight", "700")
                .set("background", "none")
                .set("cursor", "pointer");
    }

    private class AddChannelDialog extends Dialog {
        private final VerticalLayout formLayout;
        private final ComboBox<String> names = new ComboBox<>();
        private final ComboBox<String> userNames = new ComboBox<>();
        private RadioButtonGroup<String> radioButtons;


        public AddChannelDialog() {
            formLayout = new VerticalLayout();
            this.add(formLayout);
            addCloseListeners();
            createTitle();
            createRadioButtons();
            createAllComboBox();
            createValidateButton();
            open();
        }

        private void addCloseListeners() {
            setCloseOnEsc(true);
            setCloseOnOutsideClick(true);
            Button closeButton = new Button("Close");
            closeButton.addClickListener(event -> close());
            formLayout.add(closeButton);
        }

        private void createTitle() {
            H2 title = new H2("Ajouter une nouvelle conversation avec : ");
            formLayout.add(title);
        }

        private void createRadioButtons() {
            radioButtons = new RadioButtonGroup<>();
            radioButtons.setItems("nom", "pseudo");
            radioButtons.setValue("nom");
            radioButtons.setLabel("Chercher par : ");
            radioButtons.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
            radioButtons.addValueChangeListener(event -> {
                names.setVisible(event.getValue().equals("nom"));
                userNames.setVisible(event.getValue().equals("pseudo"));
            });
            formLayout.add(radioButtons);
        }

        private void createAllComboBox() {
            ArrayList<Person> users = getController().getAllUser();
            ArrayList<String> namesValues = getController().getAllNamesOrUsernames(users, true);
            createComboBox(names, namesValues);
            ArrayList<String> usernamesValues = getController().getAllNamesOrUsernames(users, false);
            createComboBox(userNames, usernamesValues);
            names.setVisible(true);
        }

        private void createValidateButton() {
            Button validate = new Button("Valider");
            validate.addClickListener(event -> {
                String value;
                if (radioButtons.getValue().equals("nom")) {
                    value = names.getValue();
                } else {
                    value = userNames.getValue();
                }
                long ok = chatController.createNewPrivateChannel(currentUser.getId(), radioButtons.getValue(), value);
                closeAndShowError(ok);
            });
            formLayout.add(validate);
        }

        private void createComboBox(ComboBox<String> comboBox, ArrayList<String> values) {
            comboBox.setItems(values);
            comboBox.setVisible(false);
            comboBox.setClearButtonVisible(true);
            formLayout.add(comboBox);
        }

        /**
         * @param ok if -1: close dialog and show error
         *           else: close dialog and opens a new one with link to the new channel
         */
        private void closeAndShowError(long ok) {
            this.close();
            if (ok == -1) {
                Notification.show("Probleme : votre conversation n'a pas pu etre creee.");
            } else {
                Notification.show("Conversation creee ! Rafraichissez la page pour vous y rendre");
            }
        }

    }

}
