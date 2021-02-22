package app.web;

import app.jpa_repo.PublicChatMessageRepository;
import app.jpa_repo.TextChannelRepository;
import app.jpa_repo.UserRepository;
import app.model.chat.PublicChatMessage;
import app.model.chat.TextChannel;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
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

import java.text.DateFormat;
import java.util.Date;
import java.util.Optional;

@Route(value = "channels", layout = MainLayout.class)
public class TextChannelView extends ComponentBuilder implements HasDynamicTitle, HasUrlParameter<Long> {

    private TextChannelRepository textChannelRepository;
    private PublicChatMessageRepository publicMessageRepository;
    private UserRepository user;

    private TextChannel textChannel;
    //TODO: créer un objet Button img path(false/true) + statut

    private ComponentButton muteMicrophone;
    private ComponentButton muteHeadphone;

    private Button sendMessage;
    private Button exitButton;

    private HorizontalLayout layout = new HorizontalLayout();

    //TODO: send message enter/button
    //TODO: sur la meme ligne
    //TODO: Change color loader

    private String[] test = {"william", "david", "laure"};

    public TextChannelView(@Autowired TextChannelRepository textChannelRepository, @Autowired PublicChatMessageRepository publicMessageRepository, @Autowired UserRepository user) {
        this.textChannelRepository = textChannelRepository;
        this.publicMessageRepository = publicMessageRepository;
        this.user = user;
        muteMicrophone = new ComponentButton("img/micOn.svg", "img/micOff.svg", "unmute microphone", "mute microphone", Key.DIGIT_1);
        muteMicrophone.addClickListener(event -> {
            muteMicrophone.changeStatus(event);
        });

        muteHeadphone = new ComponentButton("img/headsetOn.svg", "img/headsetOff.svg", "unmute headphone", "mute headphone", Key.DIGIT_2);
        muteHeadphone.addClickListener(event -> {
            muteHeadphone.changeStatus(event);
        });

        exitButton = createButtonText("Quitter", "#F04747");
        exitButton.addClickListener(event -> {
            exitButton.getStyle().set("display", "none");
            muteHeadphone.getStyle().set("display", "none");
            muteMicrophone.getStyle().set("display", "none");
        });
        sendMessage = createButtonText("Envoyer", "#000");
        sendMessage.addClickShortcut(Key.ENTER);
        sendMessage.addClickListener(event -> {

            if (!textField.isEmpty()) {
                PublicChatMessage msg = PublicChatMessage.builder()
                        .message(textField.getValue())
                        .channelid(1)
                        .parentId(1)
                        .sender(1)
                        .timeCreated(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date()))
                        .deleted(false)
                        .build();
                publicMessageRepository.save(msg);
                textField.clear();
                textField.focus();
                refresh();
            }
        });

        //exitButton.addClickListener(event -> ); hide mic and head

        layout = createLayout(
                createCard(ComponentBuilder.ColorHTML.DARKGRAY, "20%", "cardLeft", test),
                createCard(ComponentBuilder.ColorHTML.GREY, "60%", "cardCenter", test, sendMessage, muteMicrophone, muteHeadphone, exitButton),
                createCard(ComponentBuilder.ColorHTML.DARKGRAY, "20%", "cardRigth", test)
        );
        add(layout);
        refresh();
    }

    public HorizontalLayout createLayout(Component... card) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.getStyle()
                .set("position", "absolute")
                .set("top", "75px")
                .set("bottom", "0")
                .set("margin", "0")
                .set("padding", "0");
        for (Component item : card) layout.add(item);
        return layout;
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
    }


    private void refresh() {
        cardTop.removeAll();
        //TODO: ici il faut changer le 1 (qui correspond au channel id 1) par une variable qui depend de l'url
        publicMessageRepository.findAllByChannelidAndDeletedFalse(1)
                .stream()
                .map(MessageLayout::new)
                .forEach(cardTop::add);
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
                publicMessageRepository.updateDeletedById(publicMessage.getId());
            });

            message.getStyle().set("border", "none");
            message.getStyle().set("border-width", "0px");
            message.getStyle().set("outline", "none");

            message.setLabel(user.findById(publicMessage.getSender()).getUsername() + " " + String.valueOf(publicMessage.getTimeCreated()));
            add(message);
            layout.add(modification);
            layout.add(supprimer);
            add(layout);

            Binder<PublicChatMessage> binder = new Binder<>(PublicChatMessage.class);
            binder.bindInstanceFields(this);
            binder.setBean(publicMessage);
            binder.addValueChangeListener(event -> publicMessageRepository.save(binder.getBean()));
        }
    }
}

