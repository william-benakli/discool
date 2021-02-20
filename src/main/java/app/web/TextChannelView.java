package app.web;

import app.jpa_repo.PublicChatMessageRepository;
import app.jpa_repo.TextChannelRepository;
import app.model.chat.PublicChatMessage;
import app.model.chat.TextChannel;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Text;
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

import java.sql.Date;
import java.time.Instant;
import java.util.Optional;

@Route(value = "channels", layout = MainLayout.class)
public class TextChannelView extends ComponentBuilder implements HasDynamicTitle, HasUrlParameter<Long> {

    String test[] = {
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce semper sed ipsum nec vulputate. Duis gravida velit nec quam consequat semper. Vestibulum lobortis eros in dictum iaculis. Etiam mi est, rhoncus elementum leo a, maximus placerat odio. Nulla varius, lorem eleifend faucibus consequat, mi nulla placerat leo, nec suscipit nisl tellus vitae risus. Mauris a suscipit risus. Donec condimentum enim eu tortor euismod, eu porttitor libero mattis. Integer sodales, turpis vitae mollis mollis, felis mauris semper sem, in aliquet eros nisl et ante. Fusce vulputate tortor elit, et condimentum tortor dignissim vitae. Vivamus fermentum ultricies leo, ut maximus sem efficitur at. Vestibulum ultrices lacinia blandit. Mauris neque dui, varius ac magna non, tempus euismod erat. Aliquam laoreet pharetra faucibus. Proin consequat rhoncus diam, nec euismod felis gravida in.\n" +
                    "\n",
            "Quisque volutpat arcu vitae mi fringilla mattis. Cras eu massa neque. Morbi non dictum tellus. Nullam orci enim, gravida at felis sed, porttitor condimentum risus. Nam ac odio dolor. Donec sagittis sem ac ullamcorper aliquet. Aliquam erat volutpat. Mauris eros mauris, posuere vitae aliquet eu, hendrerit quis ex. Integer quam lorem, suscipit vitae ultrices sed, imperdiet non tortor.\n" +
                    "\n",
            "Ut sit amet massa mattis, auctor felis consectetur, molestie enim. Suspendisse sed malesuada turpis. Sed venenatis augue vel dolor placerat dapibus. Cras aliquam non est quis accumsan. Proin sed tincidunt odio. Curabitur eu condimentum metus. Mauris in arcu ut massa dignissim facilisis. Ut fringilla turpis mollis faucibus semper. Suspendisse ut dapibus sapien.\n" +
                    "\n",
            "Etiam mauris ipsum, tempus non placerat vitae, dignissim sit amet nunc. Sed in orci leo. Quisque eu tortor mi. Vivamus neque nibh, commodo eget purus quis, finibus ultricies lorem. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Mauris feugiat placerat lobortis. Nunc ornare lacus non enim rhoncus aliquam.\n" +
                    "\n"
    };

    private TextChannelRepository textChannelRepository;
    private PublicChatMessageRepository publicMessageRepository;

    private TextChannel textChannel;
    //TODO: créer un objet Button img path(false/true) + statut

    private ComponentButton muteMicrophone;
    private ComponentButton muteHeadphone;

    private Button sendMessage;
    private Button exitButton;
    private TextField textField = (TextField) textField();
    private VerticalLayout LayoutTest = new VerticalLayout();

    //TODO: send message enter/button
    //TODO: sur la meme ligne
    //TODO: Change color loader

    public TextChannelView(@Autowired TextChannelRepository textChannelRepository, @Autowired PublicChatMessageRepository publicMessageRepository) {
        this.textChannelRepository = textChannelRepository;
        this.publicMessageRepository = publicMessageRepository;

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

        sendMessage.addClickListener(event -> {
            System.out.println("clique ici");
            System.out.println(Date.from(Instant.now()).getTime() + " -------------------");
            publicMessageRepository.save(PublicChatMessage.builder()
                    .message(textField.getValue())
                    .channelid(1L)
                    .id(2L)
                    .parentId(2L)
                    .sender(0L)
                    .timeCreated(Date.from(Instant.now()).getTime())
                    .deleted(false)
                    .build());
            textField.focus();
            textField.clear();
            refreshZone();
        });
        //exitButton.addClickListener(event -> ); hide mic and head

        createLayout(
                createCard(ComponentBuilder.ColorHTML.DARKGRAY, "20%", "cardLeft", test),
                createCard(ComponentBuilder.ColorHTML.GREY, "60%", "cardCenter", test, sendMessage, muteMicrophone, muteHeadphone, exitButton),
                createCard(ComponentBuilder.ColorHTML.DARKGRAY, "20%", "cardRigth", test)
        );

    }

    public void createLayout(Component... card) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.getStyle()
                .set("position", "absolute")
                .set("top", "75px")
                .set("bottom", "0")
                .set("margin", "0")
                .set("padding", "0");
        for (Component item : card) layout.add(item);
        add(layout);
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


    private void refreshZone() {
        publicMessageRepository.findAll().stream()
                .map(MessageLayout::new)
                .forEach(LayoutTest::add);
    }


    class MessageLayout extends HorizontalLayout {
        Text message = new Text("");

        public MessageLayout(PublicChatMessage publicMessage) {
            add(message);
            Binder<PublicChatMessage> binder = new Binder<>(PublicChatMessage.class);
            binder.bindInstanceFields(message);
            binder.setBean(publicMessage);
            binder.addValueChangeListener(event -> publicMessageRepository.save(binder.getBean()));

        }

    }
}

