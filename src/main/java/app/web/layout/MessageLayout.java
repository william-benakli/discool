package app.web.layout;

import app.jpa_repo.PublicChatMessageRepository;
import app.jpa_repo.UserRepository;
import app.model.chat.PublicChatMessage;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

public class MessageLayout extends HorizontalLayout {

    private TextField message = new TextField();
    private VerticalLayout layout = new VerticalLayout();
    private Button supprimer;
    private Button modification;
    private PublicChatMessageRepository publicChatMessageRepository;
    private UserRepository userRepository;

    public MessageLayout(PublicChatMessage publicMessage, PublicChatMessageRepository publicChatMessageRepository,
                         UserRepository userRepository) {
        this.publicChatMessageRepository = publicChatMessageRepository;
        this.userRepository = userRepository;
        supprimer = new Button("Suppression");
        modification = new Button("Modification");

        supprimer.addClickListener(event -> {
            publicChatMessageRepository.updateDeletedById(publicMessage.getId());
        });

        message.getStyle().set("border", "none");
        message.getStyle().set("border-width", "0px");
        message.getStyle().set("outline", "none");

        message.setLabel(userRepository.findById(publicMessage.getSender()).getUsername() + " " + String.valueOf(publicMessage.getTimeCreated()));
        add(message);
        layout.add(modification);
        layout.add(supprimer);
        add(layout);

        Binder<PublicChatMessage> binder = new Binder<>(PublicChatMessage.class);
        binder.bindInstanceFields(this);
        binder.setBean(publicMessage);
        binder.addValueChangeListener(event -> publicChatMessageRepository.save(binder.getBean()));
    }
}