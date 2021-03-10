package app.web.views;

import app.controller.Controller;
import app.controller.Markdown;
import app.controller.PublicMessagesBroadcaster;
import app.jpa_repo.PersonRepository;
import app.jpa_repo.PublicChatMessageRepository;
import app.jpa_repo.TextChannelRepository;
import app.model.chat.PublicChatMessage;
import app.model.chat.TextChannel;
import app.model.users.Person;
import app.web.components.ComponentButton;
import app.web.layout.Navbar;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
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
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;


@Route(value = "channels", layout = Navbar.class)
public class TextChannelView extends ViewWithSidebars implements HasDynamicTitle, HasUrlParameter<Long> {

    private final TextChannelRepository textChannelRepository;
    private final PersonRepository personRepository;
    private TextChannel textChannel;
    private final TextField messageTextField;
    private final FlexLayout chatBar = new FlexLayout();
    private ComponentButton muteMicrophone;
    private ComponentButton muteHeadphone;
    private Button exitButton;
    private Button sendMessage;

    private FlexLayout messageContainer = new FlexLayout();

    private Registration broadcasterRegistration;


    public TextChannelView(@Autowired TextChannelRepository textChannelRepository,
                           @Autowired PublicChatMessageRepository publicChatMessageRepository,
                           @Autowired PersonRepository personRepository) {
        this.textChannelRepository = textChannelRepository;
        this.personRepository = personRepository;
        setController(new Controller(personRepository, textChannelRepository, publicChatMessageRepository,
                null, null));
        this.messageTextField = createTextField();
        createVoiceChatButtons();
        createSendMessageButton();
    }

    private void createSendMessageButton() {
        sendMessage = createButtonWithLabel("Envoyer", "#000");
        sendMessage.addClickShortcut(Key.ENTER);

        sendMessage.addClickListener(event -> {
            //     if (!messageTextField.isEmpty()) {
            // TODO : set the parentId and the userId
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String username = authentication.getName();
            Person sender = personRepository.findByUsername(username);
            PublicChatMessage newMessage = getController().saveMessage("test", textChannel.getId(), 1, sender.getId());
                messageTextField.clear();
                messageTextField.focus();
                PublicMessagesBroadcaster.broadcast("NEW_MESSAGE", new MessageLayout(newMessage));
            //     }
        });

    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        UI ui = attachEvent.getUI();
        broadcasterRegistration = PublicMessagesBroadcaster.register((type, messageLayoutPublicChatMessage) -> {
            ui.access(() -> receiveBroadcast(type, messageLayoutPublicChatMessage));
        });
    }

    private void receiveBroadcast(String type, MessageLayout messageLayout) {
        switch (type) {
            case "NEW_MESSAGE":
                messageContainer.add(messageLayout);
                break;
            case "DELETE_MESSAGE":
                messageContainer.remove(messageLayout);
                break;
            case "UPDATE_MESSAGE":
                //TODO: mettre à jour le message envoyé
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
        System.out.println("test textInPuntFile");
        return textField;
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        broadcasterRegistration.remove();
        broadcasterRegistration = null;
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
        //chatBar.removeAll();
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

        FlexLayout messageInputBar = new FlexLayout();

        messageInputBar.add(messageTextField, chatButtonContainer);

        setCardStyle(messageContainer, "60%", ColorHTML.GREY);
        messageContainer.setHeightFull();
        messageContainer.getStyle()
                .set("position", "-webkit-sticky")
                .set("position", "sticky")
                .set("bottom", "0px")
                .set("background-color", ColorHTML.GREY.getColorHtml())
                .set("flex-direction", "column");

        for (PublicChatMessage message : getController().getChatMessagesForChannel(textChannel.getId()))
            messageContainer.add(new MessageLayout(message));

        chatBar.add(messageContainer, messageInputBar);
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
        createSidebar(textChannel.getCourseId());
        createMembersBar(textChannel.getCourseId());
        createLayout(chatBar);
        createChatBar();
    }


    public class MessageLayout extends HorizontalLayout {
        private Paragraph metaData;
        private Paragraph message;
        private Button delete;
        private Button modify;

        public MessageLayout(PublicChatMessage publicMessage) {
            this.metaData = new Paragraph();
            metaData.setText(getController().getUsernameOfSender(publicMessage) + " " + publicMessage.getTimeCreated());
            metaData.getStyle().set("border", "none");
            metaData.getStyle().set("border-width", "0px");
            metaData.getStyle().set("outline", "none");
            this.add(metaData);

            this.message = new Paragraph();
            message.add(Markdown.getHtmlFromMarkdown(publicMessage.getMessage()));
            message.getStyle().set("border", "none");
            message.getStyle().set("border-width", "0px");
            message.getStyle().set("outline", "none");
            this.add(message);

            delete = new Button("Suppression");

            delete.addClickListener(event -> {
                Dialog dialog = new Dialog();
                dialog.add(new Paragraph("Voulez vous vraiment supprimer votre message ?"));
                Button oui = new Button("Oui");
                Button non = new Button("Non");
                dialog.add(non);
                dialog.add(oui);
                dialog.open();

                oui.addClickListener(ev -> {
                    getController().deleteMessage(publicMessage);
                    PublicMessagesBroadcaster.broadcast("DELETE_MESSAGE", this);
                    dialog.close();
                    Notification.show("Vous avez supprimé votre message");
                });

                non.addClickListener(ev -> {
                    dialog.close();
                });
            });

            modify = new Button("Modification");

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
                    if (!messageUpdate.getValue().equals(publicMessage.getMessage())) {
                        getController().changeMessage(publicMessage, messageUpdate.getValue());
                        PublicMessagesBroadcaster.broadcast("UPDATE_MESSAGE", this);
                        Notification.show("Vous avez modifié votre message");
                    }
                    dialog.close();
                });

                non.addClickListener(ev -> {
                    dialog.close();
                });
            });

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            long id = personRepository.findIdByUsername(authentication.getName());

            //Protection si l'utilisateur est bien le createur du message
            if (id == publicMessage.getSender()) {
                VerticalLayout layout = new VerticalLayout();
                layout.add(modify);
                layout.add(delete);
                add(layout);
            }

        }
    }


}

