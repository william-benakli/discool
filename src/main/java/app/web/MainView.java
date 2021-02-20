package app.web;

import app.jpa_repo.PublicChatMessageRepository;
import app.model.chat.PublicChatMessage;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.sql.Date;
import java.time.Instant;

@Route(value = "")
@PageTitle("Discool")
public class MainView extends ComponentBuilder {


    //TODO: créer un objet Button img path(false/true) + statut

    String[] test = {"azaaz", "azazazaz", "aqzdacac"};
    private ComponentButton muteMicrophone;
    private ComponentButton muteHeadphone;
    private Button exitButton;
    private Button sendMessage;
    private TextField textField = (TextField) textField();
    private PublicChatMessageRepository repository;
    private VerticalLayout LayoutTest = new VerticalLayout();

    //TODO: send message enter/button
    //TODO: sur la meme ligne
    //TODO: Change color loader

    public MainView(PublicChatMessageRepository repository) {
        this.repository = repository;

        muteMicrophone = new ComponentButton("img/micOn.svg", "img/micOff.svg", "unmute microphone", "mute microphone", Key.DIGIT_1);
        muteMicrophone.addClickListener(event -> {
            muteMicrophone.changeStatus(event);
        });

        muteHeadphone = new ComponentButton("img/headsetOn.svg", "img/headsetOff.svg", "unmute headphone", "mute headphone", Key.DIGIT_2);
        muteHeadphone.addClickListener(event -> {
            muteHeadphone.changeStatus(event);
        });

        exitButton = createButtonText("Quitter", "#F04747");
        sendMessage = createButtonText("Envoyer", "#000");

        /*exitButton.addClickListener(event -> {
            exitButton.getStyle().set("display","none");
            muteHeadphone.getStyle().set("display","none");
            muteMicrophone.getStyle().set("display","none");
        });*/

        sendMessage.addClickListener(event -> {
            System.out.println("clique ici");
            System.out.println(Date.from(Instant.now()).getTime() + " -------------------");
            repository.save(PublicChatMessage.builder()
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
        add(LayoutTest);
    }

    private void refreshZone() {
        repository.findAll().stream()
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
            binder.addValueChangeListener(event -> repository.save(binder.getBean()));

        }

    }
}
