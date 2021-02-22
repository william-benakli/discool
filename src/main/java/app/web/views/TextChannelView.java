package app.web.views;

import app.controller.Controller;
import app.jpa_repo.PublicChatMessageRepository;
import app.jpa_repo.TextChannelRepository;
import app.jpa_repo.UserRepository;
import app.model.chat.PublicChatMessage;
import app.model.chat.TextChannel;
import app.web.components.ComponentButton;
import app.web.layout.CourseLayout;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Optional;


@Route(value = "channels", layout = CourseLayout.class)
public class TextChannelView extends ViewWithSidebars implements HasDynamicTitle, HasUrlParameter<Long> {
    private final TextChannelRepository textChannelRepository;
    private TextChannel textChannel;
    private final TextField messageTextField;
    private final FlexLayout chatBar = new FlexLayout();
    private ComponentButton muteMicrophone;
    private ComponentButton muteHeadphone;
    private Button exitButton;
    private Button sendMessage;


    public TextChannelView(@Autowired TextChannelRepository textChannelRepository,
                           @Autowired PublicChatMessageRepository publicChatMessageRepository,
                           @Autowired UserRepository userRepository) {
        this.textChannelRepository = textChannelRepository;
        setController(new Controller(userRepository, textChannelRepository, publicChatMessageRepository,
                                     null, null));
        messageTextField = createTextField();
        createVoiceChatButtons();
        createSendMessageButton();
    }

    @Override
    public String getPageTitle() {
        return textChannel.getName();
    }

    public TextField createTextField() {
        TextField textField = new TextField();
        textField.setPlaceholder("Envoyer un message");
        textField.setWidthFull();
        //textField.addFocusShortcut(Key.KEY_T, KeyModifier.ALT);
        textField.getStyle().set("margin", "0 2.5px");
        return textField;
    }

    private void createSendMessageButton() {
        sendMessage = createButtonWithLabel("Envoyer", "#000");
        sendMessage.addClickShortcut(Key.ENTER);

        sendMessage.addClickListener(event -> {
            if (!messageTextField.isEmpty()) {
                // TODO : set the parentId and the userId depending on context
                getController().saveMessage(messageTextField.getValue(), textChannel.getId(), 1, 1);
                messageTextField.clear();
                messageTextField.focus();
                refresh();
            }
        });
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

    public void refresh() {
        createChatBar();
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

        FlexLayout messageInputBar = new FlexLayout();
        messageInputBar.add(
                messageTextField,
                chatButtonContainer
        );

        FlexLayout messageContainer = new FlexLayout();
        setCardStyle(messageContainer, "60%", ColorHTML.GREY);
        messageContainer.setHeightFull();
        messageContainer.getStyle()
                .set("position", "-webkit-sticky")
                .set("position", "sticky")
                .set("bottom", "0px")
                .set("background-color", ColorHTML.GREY.getColorHtml());

        ArrayList<PublicChatMessage> messageList = getController().getChatMessagesForChannel(textChannel.getId());
        for (PublicChatMessage message : messageList) {
            MessageLayout messageLayout = new MessageLayout(message);
            messageContainer.add(messageLayout);
        }
        messageContainer.getStyle().set("flex-direction", "column");
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
        Optional<TextChannel> c = textChannelRepository.findById(parameter);
        if (c.isPresent()) {
            textChannel = c.get();
        } else {
            throw new Exception("There is no channel with this ID.");
            // TODO : take care of the exception
        }
        createSidebar(textChannel.getCourseId());
        createMembersBar(textChannel.getCourseId());
        createChatBar();
        createLayout(chatBar);
    }

    class MessageLayout extends HorizontalLayout {
        private TextField message = new TextField();
        private VerticalLayout layout = new VerticalLayout();
        private Button supprimer;
        private Button modification;

        public MessageLayout(PublicChatMessage publicMessage) {
            supprimer = new Button("Suppression");
            modification = new Button("Modification");

            supprimer.addClickListener(event -> {
                getController().deleteMessage(publicMessage);
            });

            message.getStyle().set("border", "none");
            message.getStyle().set("border-width", "0px");
            message.getStyle().set("outline", "none");

            message.setLabel(getController().getUsernameOfSender(publicMessage) + " " + publicMessage.getTimeCreated());
            add(message);
            layout.add(modification);
            layout.add(supprimer);
            add(layout);

            Binder<PublicChatMessage> binder = new Binder<>(PublicChatMessage.class);
            binder.bindInstanceFields(this);
            binder.setBean(publicMessage);
            binder.addValueChangeListener(event -> getController().saveMessage(binder.getBean()));

        }
    }


}

