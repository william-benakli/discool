package app.web.views;

import app.controller.AssignmentController;
import app.controller.Controller;
import app.controller.Markdown;
import app.controller.PublicMessagesBroadcaster;
import app.controller.commands.CommandsClearChat;
import app.jpa_repo.*;
import app.model.chat.PublicChatMessage;
import app.model.chat.TextChannel;
import app.model.users.Person;
import app.web.components.ComponentButton;
import app.web.layout.Navbar;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;


@Route(value = "channels", layout = Navbar.class)
public class TextChannelView extends ViewWithSidebars implements HasDynamicTitle, HasUrlParameter<Long> {

    private final TextChannelRepository textChannelRepository;
    private final PersonRepository personRepository;
    private final CourseRepository courseRepository;
    private TextChannel textChannel;
    private final TextField messageTextField;
    private final FlexLayout chatBar = new FlexLayout();

    private final Person currentUser;

    private long targetResponseMessage;
    private final FlexLayout messageContainer = new FlexLayout();
    private Registration broadcasterRegistration;

    private MessageResponsePopComponent selectRep;
    private Button exitButton;
    private Button sendMessage;
    private ComponentButton muteMicrophone;
    private ComponentButton muteHeadphone;


    public TextChannelView(@Autowired TextChannelRepository textChannelRepository,
                           @Autowired PublicChatMessageRepository publicChatMessageRepository,
                           @Autowired PersonRepository personRepository,
                           @Autowired AssignmentRepository assignmentRepository,
                           @Autowired CourseRepository courseRepository,
                           @Autowired StudentAssignmentsUploadsRepository studentAssignmentsUploadsRepository,
                           @Autowired GroupRepository groupRepository,
                           @Autowired GroupMembersRepository groupMembersRepository,
                           @Autowired MoodlePageRepository moodlePageRepository) {
        this.textChannelRepository = textChannelRepository;
        this.personRepository = personRepository;
        this.courseRepository = courseRepository;
        this.targetResponseMessage = 0;
        setPersonRepository(personRepository);
        setController(new Controller(personRepository, textChannelRepository, publicChatMessageRepository,
                                     courseRepository, moodlePageRepository, groupRepository, groupMembersRepository));
        setAssignmentController(new AssignmentController(personRepository, assignmentRepository,
                                                         studentAssignmentsUploadsRepository, courseRepository));
        this.messageTextField = createTextField();
        createVoiceChatButtons();
        createSendMessageButton();
        scrollDownChat();
    }

    private void createSendMessageButton() {
        sendMessage = createButtonWithLabel("Envoyer", "#000");
        sendMessage.addClickShortcut(Key.ENTER);

        sendMessage.addClickListener(event -> {
            if (!messageTextField.isEmpty()) {
                if (!messageTextField.getValue().startsWith("/")) {

                    PublicChatMessage newMessage;

                    if (targetResponseMessage == 0) {
                        newMessage = getController().saveMessage(messageTextField.getValue(), textChannel.getId(), 1, currentUser.getId());
                    } else {
                        newMessage = getController().saveMessage(messageTextField.getValue(), textChannel.getId(), targetResponseMessage, currentUser.getId());
                        if (newMessage != null) {
                            selectRep.changeStatus();
                            targetResponseMessage = 0;
                        }
                    }
                    PublicMessagesBroadcaster.broadcast("NEW_MESSAGE", newMessage);
                    scrollDownChat();

                } else {
                    String[] arg = messageTextField.getValue().split(" ");
                    switch (arg[0]) {
                        case "/clear":
                            new CommandsClearChat(this.getController(), currentUser.getId(), textChannel.getId(), arg);
                            break;
                    }
                    //   PublicMessagesBroadcaster.broadcast("UPDATE_ALL", new MessageLayout(newMessage));
                    Notification.show("Vous executez une commande");

                }
                messageTextField.clear();
                messageTextField.focus();
            }
        });
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        UI ui = attachEvent.getUI();
        broadcasterRegistration = PublicMessagesBroadcaster.register((type, publicChatMessage) -> {
            if (ui.isEnabled() && ui.getUI().isPresent()) {
                ui.access(() -> receiveBroadcast(type, publicChatMessage));
            }
        });
    }

    private void receiveBroadcast(String type, PublicChatMessage message) {
        switch (type) {
            case "NEW_MESSAGE":
                UI.getCurrent().access(() -> {
                    messageContainer.add(new MessageLayout(message));
                });
                break;
            case "DELETE_MESSAGE":
                UI.getCurrent().access(() -> {
                    for (Object obj : messageContainer.getChildren().toArray()) {
                        if (obj instanceof MessageLayout
                                && ((MessageLayout) obj).getPublicChatMessage().getId() == message.getId()) {
                            // we found the layout to update
                            ((MessageLayout) obj).setVisible(false);
                        }
                    }
                });
                break;
            case "UPDATE_MESSAGE":
                UI.getCurrent().access(() -> {
                    for (Object obj : messageContainer.getChildren().toArray()) {
                        if (obj instanceof MessageLayout
                                && ((MessageLayout) obj).getPublicChatMessage().getId() == message.getId()) {
                            // we found the layout to update
                            ((MessageLayout) obj).updateMessage();
                        }
                    }
                });
                break;
            case "UPDATE_ALL":
                UI.getCurrent().access(() -> {
                    messageContainer.removeAll();
                    for (PublicChatMessage publicChatMessage : getController().getChatMessagesForChannel(textChannel.getId())) {
                        System.out.println("Message" + publicChatMessage.getMessage());
                        messageContainer.add(new MessageLayout(publicChatMessage));
                    }
                });
                break;
            default:
                break;
        }
    }

    @Override
    public String getPageTitle() {
        return textChannel.getName();
    }

    public TextField createTextField() {
        TextField textField = new TextField();
        textField.setVisible(true);
        textField.getStyle().set("color", "blue");
        textField.setPlaceholder("Envoyer un message");
        textField.setWidthFull();
        textField.addFocusShortcut(Key.KEY_T, KeyModifier.ALT);
        textField.getStyle().set("margin", "0 2.5px");
        return textField;
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        broadcasterRegistration.remove();
        broadcasterRegistration = null;
    }

    public void scrollDownChat() {
        messageContainer.getElement().executeJs("this.scrollTop = this.scrollHeight;");
    }

    private void createVoiceChatButtons() {
        muteMicrophone = new ComponentButton("img/micOn.svg", "img/micOff.svg", "unmute microphone", "mute microphone", Key.DIGIT_1);
        muteMicrophone.addClickListener(muteMicrophone::changeStatus);
        muteHeadphone = new ComponentButton("img/headsetOn.svg", "img/headsetOff.svg", "unmute headphone", "mute headphone", Key.DIGIT_2);
        muteHeadphone.addClickListener(muteHeadphone::changeStatus);

        exitButton = createButtonWithLabel("Quitter", "#F04747");
        exitButton.addClickListener(event -> {
            exitButton.getStyle().set("display", "none");
            muteHeadphone.getStyle().set("display", "none");
            muteMicrophone.getStyle().set("display", "none");
        });
    }

    public void createChatBar() {
        chatBar.removeAll();
        chatBar.getStyle()
                .set("overflow", "auto")
                .set("width", "60%")
                .set("margin", "0px")
                .set("background", ColorHTML.GREY.getColorHtml())
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("padding", "10px");

        FlexLayout chatButtonContainer = new FlexLayout();
        chatButtonContainer.getStyle().set("padding", "0 2.5px");
        chatButtonContainer.add(sendMessage, muteMicrophone, muteHeadphone, exitButton);

        VerticalLayout layoutMaster = new VerticalLayout();
        HorizontalLayout messageInputBar = new HorizontalLayout();
        messageInputBar.add(messageTextField, chatButtonContainer);
        setCardStyle(messageContainer, "99%", ColorHTML.GREY);
        messageContainer.setHeightFull();
        messageContainer.getStyle()
                .set("position", "-webkit-sticky")
                .set("position", "sticky")
                .set("bottom", "0px")
                .set("scrollbar-width", "thin")
                .set("scrollbar-color", ColorHTML.DARKGREY.getColorHtml())
                .set("background-color", ColorHTML.GREY.getColorHtml())
                .set("flex-direction", "column");

        selectRep = new MessageResponsePopComponent();

        for (PublicChatMessage message : getController().getChatMessagesForChannel(textChannel.getId())) {
            if (!message.isDeleted()) messageContainer.add(new MessageLayout(message));
        }
        layoutMaster.getStyle().set("display", "block");
        layoutMaster.add(selectRep, messageInputBar);
        chatBar.add(messageContainer, layoutMaster);
    }


    public class MessageResponsePopComponent extends Div {
        HorizontalLayout layoutHorizontalLayout;

        Paragraph message;
        Button annuler;

        public MessageResponsePopComponent() {
            annuler = new Button("X");
            message = new Paragraph();
            message.getStyle().set("padding-right", "70%").set("padding", "10px");
            layoutHorizontalLayout = new HorizontalLayout();
            getStyle().set("background-color", ColorHTML.DARKGREY.getColorHtml());
            setVisible(false);
            setStyle();
            eventClickMessage();
            layoutHorizontalLayout.add(message, annuler);
            add(layoutHorizontalLayout);
        }

        public void setStyle() {
            this.getStyle().set("-webkit-border-top-left-radius", "30px")
                    .set("-webkit-border-top-right-radius", "30px")
                    .set("-moz-border-radius-topleft", "30px")
                    .set("-moz-border-radius-topright", "30px")
                    .set("border-top-left-radius", "30px")
                    .set("border-top-right-radius", "30px")
                    .set("box-shadow", " -2px -24px 46px -27px rgba(0,0,0,0.43)")
                    .set("-webkit-box-shadow", "-2px -24px 46px -27px rgba(0,0,0,0.43)")
                    .set("-moz-box-shadow", "-2px -24px 46px -27px rgba(0,0,0,0.43)");
        }

        public void setMessage(String userTo) {
            message.setText("Repondre à " + userTo);
        }

        public void eventClickMessage() {
            annuler.addClickListener(ev -> {
                targetResponseMessage = 0L;
                changeStatus();
            });
        }

        public void changeStatus() {
            setVisible((!this.isVisible()));
        }

        public void show() {
            setVisible(true);
            messageTextField.focus();
        }

    }

    /**
     * Creates a Button with a label (in white) and a background color
     *
     * @param label The label of the button
     * @param color The color of the background
     * @return a Button with the label and the color
     */
    private Button createButtonWithLabel(String label, String color) {
        Button button = new Button(label);
        button.getStyle()
                .set("background-color", color)
                .set("color", ColorHTML.WHITE.getColorHtml());
        return button;
    }

    @SneakyThrows // so that javac doesn't complain about dirty exception throwing
    @Override
    public void setParameter(BeforeEvent event, Long parameter) {
        Optional<TextChannel> channel = textChannelRepository.findById(parameter);
        if (channel.isPresent()) {
            textChannel = channel.get();
        } else {
            throw new Exception("There is no channel with this ID.");
            // TODO : take care of the exception
        }
        Optional<Course> c = courseRepository.findById(textChannel.getCourseId());
        setCourse(c.orElse(null));
        createSidebar(textChannel.getCourseId());
        createMembersBar(textChannel.getCourseId());
        createLayout(chatBar);
        createChatBar();
    }

    public class MessageLayout extends HorizontalLayout {
        private final int SIZEWIDTH = 25;
        private final int SIZEHEIGHT = 15;

        @Getter
        private final PublicChatMessage publicChatMessage;

        private final Div messageFullWithResponseLayout;
        private final HorizontalLayout messageFullLayout;
        private final VerticalLayout chatUserInformation;
        private final HorizontalLayout optionsUser;
        private final FlexLayout optionMenu;
        private final PopAbsoluteLayout layoutPop;

        private Paragraph metaData;
        private Paragraph message;
        private Paragraph messageReponse;
        private Image profilPicture;

        private Button response;
        private Button delete;
        private Button modify;

        public MessageLayout(PublicChatMessage publicMessage) {
            this.publicChatMessage = publicMessage;
            this.chatUserInformation = new VerticalLayout();
            setPadding(false);
            setSpacing(false);
            this.layoutPop = new PopAbsoluteLayout();
            this.optionMenu = new FlexLayout();
            this.optionsUser = new HorizontalLayout();
            this.messageFullLayout = new HorizontalLayout();
            this.messageFullWithResponseLayout = new Div();

            optionsUser.setSpacing(false);
            optionsUser.setPadding(false);
            onHover();
            createResponseMessage(publicMessage);
            createPictureSetting();
            createDeleteButton(publicMessage);
            createModifyButton(publicMessage);
            createResponseButton(publicMessage);
            createPopMessage(publicMessage);
            createChatBlock(publicMessage);
            messageFullLayout.add(profilPicture);
            messageFullLayout.add(chatUserInformation);

            messageFullWithResponseLayout.add(messageFullLayout);

            messageFullWithResponseLayout.getStyle().set("padding", "5px");

            add(messageFullWithResponseLayout);
            add(layoutPop);
        }

        private void createResponseMessage(PublicChatMessage publicMessage) {
            if (publicMessage.getParentId() != 1) {
                PublicChatMessage messageParent = getController().getMessageById(publicMessage.getParentId());
                if (messageParent != null) {
                    if (!messageParent.isDeleted()) {
                        messageReponse = new Paragraph("Reponse à " + personRepository.findById(messageParent.getSender()).getUsername() + " | "
                                                               + ((messageParent.getMessage().length() > 50) ? messageParent.getMessage().substring(0, 50) + "..." : messageParent.getMessage()));
                    } else {
                        messageReponse = new Paragraph("Message suprimée par l'utilisateur");
                    }
                    messageFullWithResponseLayout.add(messageReponse);
                }
            }
        }

        private void createDeleteButton(PublicChatMessage publicChatMessage) {
            delete = new ComponentButton("img/corbeille.svg", "Supprimer", SIZEWIDTH, SIZEHEIGHT);

            delete.addClickListener(event -> {
                Dialog dialog = new Dialog();
                dialog.add(new Paragraph("Voulez vous vraiment supprimer votre message ?"));
                Button oui = new Button("Oui");
                Button non = new Button("Non");

                dialog.add(oui);
                dialog.add(non);
                dialog.open();

                oui.addClickListener(ev -> {
                    getController().deleteMessage(publicChatMessage);
                    PublicMessagesBroadcaster.broadcast("DELETE_MESSAGE", publicChatMessage);
                    dialog.close();
                    Notification.show("Vous avez supprimé votre message");
                });

                non.addClickListener(ev -> dialog.close());
            });
        }

        private void createModifyButton(PublicChatMessage publicMessage) {
            modify = new ComponentButton("img/editer.svg", "Editer", SIZEWIDTH, SIZEHEIGHT);

            modify.addClickListener(event -> {
                Dialog dialog = new Dialog();

                Button oui = new Button("Valider");
                Button non = new Button("Annuler");

                TextField messageUpdate = new TextField();
                messageUpdate.setValue(message.getText());
                dialog.add(new Paragraph("Modifiez votre message ?"));
                dialog.add(messageUpdate);

                dialog.add(oui);
                dialog.add(non);
                dialog.open();

                oui.addClickListener(ev -> {
                    if (!messageUpdate.getValue().equals(publicMessage.getMessage()) && !messageUpdate.isEmpty()) {
                        getController().changeMessage(publicMessage, messageUpdate.getValue());
                        this.message.setText(messageUpdate.getValue());
                        PublicMessagesBroadcaster.broadcast("UPDATE_MESSAGE", publicMessage);
                        Notification.show("Vous avez modifié votre message");
                    }
                    dialog.close();
                });

                non.addClickListener(ev -> dialog.close());
            });
        }

        public void createResponseButton(PublicChatMessage publicMessage) {
            response = new ComponentButton("img/repondre.svg", "Repondre", SIZEWIDTH, SIZEHEIGHT);
            response.addClickListener(ev -> {
                if (publicMessage != null) {
                    targetResponseMessage = publicMessage.getId();
                    selectRep.setMessage(personRepository.findById(publicMessage.getSender()).getUsername());
                    if (!selectRep.isVisible()) selectRep.show();
                    scrollDownChat();
                }
            });
        }

        public void createPopMessage(PublicChatMessage publicMessage) {
            optionsUser.add(response);

            //Protection si l'utilisateur est bien le createur du message
            if (currentUser.getId() == publicMessage.getSender()) {
                optionsUser.add(modify);
                optionsUser.add(delete);
            }
            layoutPop.add(optionsUser);
            layoutPop.resize();

            messageFullLayout.add(optionMenu);
        }

        private void createChatBlock(PublicChatMessage publicMessage) {
            this.metaData = createParagrapheAmelioration(getController().getUsernameOfSender(publicMessage) + " | "
                                                                 + getController().convertLongToDate(publicMessage.getTimeCreated()));
            metaData.getStyle()
                    .set("color", ColorHTML.PURPLE.getColorHtml())
                    .set("font-weight", "700");
            chatUserInformation.add(metaData);
            this.message = createParagrapheAmelioration(publicMessage.getMessage());
            chatUserInformation.add(message);
        }

        private void createPictureSetting(long senderId) {
            Person sender = personRepository.findById(senderId);
            this.profilPicture = sender.getProfilePicture();
            this.profilPicture.setWidth("60px");
            this.profilPicture.setHeight("60px");
            this.profilPicture.getStyle()
                    .set("border-radius", "40px")
                    .set("margin", "0px 0px 0px 0px");
        }

        private Paragraph createParagrapheAmelioration(String text) {
            Paragraph data = new Paragraph();
            data.add(Markdown.getHtmlFromMarkdown(text));
            data.getStyle()
                    .set("border", "none")
                    .set("border-width", "0px")
                    .set("outline", "none")
                    .set("margin", "0")
                    .set("padding", "0");
            return data;
        }

        public void updateMessage() {
            message.removeAll();
            message.add(Markdown.getHtmlFromMarkdown(publicChatMessage.getMessage()));
        }

        private void onHover() {
            getElement().addEventListener("mouseover", e -> {
                this.getStyle().set("background-color", ColorHTML.DARKGREY.getColorHtml());
                layoutPop.setVisible(true);
            });
            getElement().addEventListener("mouseleave", e -> {
                this.getStyle().set("background-color", ColorHTML.GREY.getColorHtml());
                layoutPop.setVisible(false);
            });
        }

        private class PopAbsoluteLayout extends Div {

            public PopAbsoluteLayout() {
                this.getElement().getStyle()
                        .set("position", "absolute")
                        .set("z-index", "1")
                        .set("box-shadow", "-8px 12px 9px -5px rgba(0,0,0,0.20)")
                        .set("background-color", ColorHTML.GREY.getColorHtml())
                        .set("right", "25px")
                        .set("border-radius", "5px");
            }

            public void resize() {
                //Le nombre d elements mutiliplie par la taille definie
                setWidth(SIZEWIDTH * this.getElement().getChildren().findAny().get().getChildCount() + 20 + "px");
                setHeight((SIZEHEIGHT + 2) + "px");
                setVisible(false);
            }

        }

    }
}

