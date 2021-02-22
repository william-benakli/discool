package app.web.views;

import app.jpa_repo.PublicChatMessageRepository;
import app.jpa_repo.TextChannelRepository;
import app.jpa_repo.UserRepository;
import app.model.chat.PublicChatMessage;
import app.model.chat.TextChannel;
import app.web.components.ComponentBuilder;
import app.web.components.ComponentButton;
import app.web.layout.CourseLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.DateFormat;
import java.util.Date;
import java.util.Optional;

@Route(value = "channels", layout = CourseLayout.class)
public class TextChannelView extends VerticalLayout implements HasDynamicTitle, HasUrlParameter<Long> {
    private final PublicChatMessageRepository publicMessageRepository;
    private final UserRepository userRepository;
    private final TextChannelRepository textChannelRepository;
    private TextChannel textChannel;

    private final ComponentButton muteMicrophone;
    private final ComponentButton muteHeadphone;
    private final Button exitButton;
    private final Button sendMessage;
    private final TextField textField;

    private Component sidebar;
    private Component chat;
    private Component membersBar;

    public TextChannelView(@Autowired TextChannelRepository textChannelRepository, @Autowired PublicChatMessageRepository publicMessageRepository, @Autowired UserRepository userRepository) {
        this.textChannelRepository = textChannelRepository;
        this.publicMessageRepository = publicMessageRepository;
        this.userRepository = userRepository;
        textField = ComponentBuilder.createTextField();
        muteMicrophone = new ComponentButton("img/micOn.svg", "img/micOff.svg", "unmute microphone", "mute microphone", Key.DIGIT_1);
        muteMicrophone.addClickListener(muteMicrophone::changeStatus);

        muteHeadphone = new ComponentButton("img/headsetOn.svg", "img/headsetOff.svg", "unmute headphone", "mute headphone", Key.DIGIT_2);
        muteHeadphone.addClickListener(muteHeadphone::changeStatus);

        exitButton = ComponentBuilder.createButtonText("Quitter", "#F04747");
        exitButton.addClickListener(event -> {
            exitButton.getStyle().set("display", "none");
            muteHeadphone.getStyle().set("display", "none");
            muteMicrophone.getStyle().set("display", "none");
        });

        sendMessage = ComponentBuilder.createButtonText("Envoyer", "#000");
        sendMessage.addClickShortcut(Key.ENTER);
        sendMessage.addClickListener(event -> {
            if (!textField.isEmpty()) {
                PublicChatMessage msg = PublicChatMessage.builder()
                        .message(textField.getValue())
                        .channelid(1)
                        .parentId(1)
                        .sender(1)
                        .timeCreated(Long.parseLong(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date())))
                        .deleted(false)
                        .build();
                publicMessageRepository.save(msg);
                textField.clear();
                textField.focus();
                refresh();
            }
        });
    }

    @Override
    public String getPageTitle() {
        return textChannel.getName();
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
        makeLayout();
    }

    private void refresh() {
        sidebar = ComponentBuilder.createSideBar("20%", textChannel.getCourseId(), textChannelRepository);
        chat = ComponentBuilder.createMessageCard(ComponentBuilder.ColorHTML.GREY, "60%", "cardCenter",
                                                  publicMessageRepository.findAllByChannelid(textChannel.getId()),
                                                  publicMessageRepository, userRepository, sendMessage,
                                                  muteMicrophone, muteHeadphone, exitButton);
        membersBar = ComponentBuilder.createMembersCard(ComponentBuilder.ColorHTML.DARKGRAY, "20%");
    }

    private void makeLayout() {
        sidebar = ComponentBuilder.createSideBar("20%", textChannel.getCourseId(), textChannelRepository);
        chat = ComponentBuilder.createMessageCard(ComponentBuilder.ColorHTML.GREY, "60%", "cardCenter",
                                                  publicMessageRepository.findAllByChannelid(textChannel.getId()),
                                                  publicMessageRepository, userRepository,
                                                  sendMessage, muteMicrophone, muteHeadphone, exitButton);
        membersBar = ComponentBuilder.createMembersCard(ComponentBuilder.ColorHTML.DARKGRAY, "20%");
        HorizontalLayout layout = ComponentBuilder.createLayout(sidebar, chat, membersBar);
        this.add(layout);
    }

}

