package app.web.views;

import app.jpa_repo.*;
import app.model.chat.PrivateTextChannel;
import app.model.users.Person;
import app.web.layout.Navbar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletRequest;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
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
        messageInputBar.setWidthFull();
        chatBar.setWidth("100%");
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
        Button button = new Button("Ajouter", new Icon(VaadinIcon.PLUS_CIRCLE));
        button.getStyle()
                .set("color",ColorHTML.WHITE.getColorHtml())
                .set("background-color",ColorHTML.PURPLE.getColorHtml());
        button.addClickListener(event -> {
            AddChannelDialog dialog = new AddChannelDialog();
            dialog.setHeight("50%");
            dialog.setWidth("60%");
            dialog.open();
        });
        return button;
    }

    private RouterLink createChannelButton(PrivateTextChannel channel) {
        Person otherPerson = getOtherPerson(channel);
        RouterLink routerLink = new RouterLink("", PrivateTextChannelView.class, channel.getId());
        Button button = new Button(styleStatusUsers(otherPerson, channel.getId()));
        styleButton(routerLink, button);
        return routerLink;
    }

    @SneakyThrows
    private String getUrl(){
        VaadinServletRequest req = (VaadinServletRequest) VaadinService.getCurrentRequest();
        StringBuffer uriString = req.getRequestURL();
        URI uri = new URI(uriString.toString());
        return uri.toString();
    }

    /**
     *Take a user as a parameter and display his information
     *
     * @param p The user and his information
     * @return a card containing a user and his connection status
     */
    public FlexLayout styleStatusUsers(Person p, long id){
        String s = getUrl();
        String[] s2=s.split("/");
        FlexLayout divUser = new FlexLayout();
        FlexLayout div = new FlexLayout();
        div.getStyle()
                .set("display","flex")
                .set("flex-direction","column")
                .set("margin","5px 0");
        Paragraph pseudo = new Paragraph(p.getUsername());
        pseudo.getStyle()
                .set("color",(Long.parseLong(s2[s2.length-1])==id)?ColorHTML.PURPLE.getColorHtml():ColorHTML.TEXTGREY.getColorHtml())
                .set("font-weight","700")
                .set("margin","0")
                .set("margin-top","5px")
                .set("text-align","left");
        FlexLayout divstatus = new FlexLayout();
        Paragraph status = new Paragraph((p.isConnected())?"En ligne":"Hors-ligne");
        status.getStyle()
                .set("color", ColorHTML.TEXTGREY.getColorHtml())
                .set("margin","0");
        Image img = new Image((p.isConnected())?"img/dotgreen.svg":"img/dotred.svg", "Statut");
        img.getStyle()
                .set("margin-right","5px")
                .set("margin-top","-2.5px");
        divstatus.add(img);
        divstatus.add(status);
        div.add(pseudo);
        div.add(divstatus);
        Image iconUser = p.getProfilePicture();
        iconUser.getStyle()
                .set("width","50px")
                .set("height","50px")
                .set("margin","10px")
                .set("border-radius","25px");

        divUser.add(iconUser);
        divUser.add(div);
        return divUser;
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
                .set("background", "none")
                .set("margin-left", "3px")
                .set("margin-top","0");
        button.getStyle()
                .set("font-weight", "700")
                .set("background", "none")
                .set("cursor", "pointer");
        button.setHeight("70px");
    }

    private class AddChannelDialog extends Dialog {
        private final VerticalLayout formLayout;
        private final ComboBox<String> names = new ComboBox<>();
        private final ComboBox<String> userNames = new ComboBox<>();
        private RadioButtonGroup<String> radioButtons;


        public AddChannelDialog() {
            formLayout = new VerticalLayout();
            this.add(formLayout);
            this.setMaxHeight("420px");
            this.setMaxWidth("570px");
            createTitle();
            createRadioButtons();
            createAllComboBox();
            createButton();
            open();
        }

        private void createButton(){
            Div div=new Div();
            div.add(addCloseListeners(), createValidateButton());
            div.getStyle()
                    .set("flex-direction","row")
                    .set("padding-top","25px");
            formLayout.add(div);
        }

        private Button addCloseListeners() {
            setCloseOnEsc(true);
            setCloseOnOutsideClick(true);
            Button closeButton = new Button("Close");
            closeButton.addClickListener(event -> close());
            closeButton.getStyle()
                    .set("margin-right","2.5px")
                    .set("background-color",ColorHTML.PURPLE.getColorHtml())
                    .set("color",ColorHTML.WHITE.getColorHtml());
            return closeButton;
        }

        private void createTitle() {
            H2 title = new H2("Ajouter une nouvelle conversation : ");
            title.getStyle().set("color",ColorHTML.PURPLE.getColorHtml());
            formLayout.add(title);
        }

        private void createRadioButtons() {
            radioButtons = new RadioButtonGroup<>();
            radioButtons.setItems("nom", "pseudo");
            radioButtons.setValue("nom");
            radioButtons.setLabel("Chercher par : ");
            radioButtons.addValueChangeListener(event -> {
                names.setVisible(event.getValue().equals("nom"));
                userNames.setVisible(event.getValue().equals("pseudo"));
            });
            formLayout.add(radioButtons);
        }

        private void createAllComboBox() {
            ArrayList<Person> users = getController().getAllUsers();
            ArrayList<String> namesValues = getController().getAllNamesOrUsernames(users, true);
            createComboBox(names, namesValues);
            ArrayList<String> usernamesValues = getController().getAllNamesOrUsernames(users, false);
            createComboBox(userNames, usernamesValues);
            names.setVisible(true);
        }

        private Button createValidateButton() {
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
            validate.getStyle()
                    .set("margin-left","2.5px")
                    .set("background-color",ColorHTML.PURPLE.getColorHtml())
                    .set("color",ColorHTML.WHITE.getColorHtml());
            return validate;
        }

        private void createComboBox(ComboBox<String> comboBox, ArrayList<String> values) {
            comboBox.setItems(values);
            comboBox.setVisible(false);
            comboBox.setClearButtonVisible(true);
            comboBox.getStyle().set("margin","0");
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
