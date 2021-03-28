package app.web.views;

import app.controller.Controller;
import app.controller.PublicMessagesBroadcaster;
import app.controller.commands.CommandsClearChat;
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
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.text.SimpleDateFormat;
import java.util.Date;
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
    private long targetResponseMessage;
    private final FlexLayout messageContainer = new FlexLayout();
    private Registration broadcasterRegistration;

    public TextChannelView(@Autowired TextChannelRepository textChannelRepository,
                           @Autowired PublicChatMessageRepository publicChatMessageRepository,
                           @Autowired PersonRepository personRepository) {
        this.textChannelRepository = textChannelRepository;
        this.personRepository = personRepository;
        this.targetResponseMessage = 0;
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
            if (!messageTextField.isEmpty()) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String username = authentication.getName();
                Person sender = personRepository.findByUsername(username);

                if (!messageTextField.getValue().startsWith("/")) {

                    PublicChatMessage newMessage;

                    if (targetResponseMessage == 0) {
                        newMessage = getController().saveMessage(messageTextField.getValue(), textChannel.getId(), 1, sender.getId());
                        MessageLayout message = new MessageLayout(newMessage);
                        PublicMessagesBroadcaster.broadcast("NEW_MESSAGE", message);
                    } else {
                        newMessage = getController().saveMessage(messageTextField.getValue(), textChannel.getId(), targetResponseMessage, sender.getId());
                        MessageLayout message = new MessageLayout(newMessage);
                        PublicMessagesBroadcaster.broadcast("NEW_MESSAGE", message);
                        targetResponseMessage = 0;
                    }
                    messageContainer.getElement().executeJs("this.scrollTop = this.scrollHeight;");

                } else {
                    String[] arg = messageTextField.getValue().split(" ");
                    switch (arg[0]) {
                        case "/clear":
                            new CommandsClearChat(this.getController(), sender.getId(), textChannel.getId(), arg);
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
        broadcasterRegistration = PublicMessagesBroadcaster.register((type, messageLayoutPublicChatMessage) -> {
            if (ui.isEnabled() && ui.getUI().isPresent()) {
                ui.access(() -> receiveBroadcast(type, messageLayoutPublicChatMessage));
            }
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
                for (Object obj : messageContainer.getChildren().toArray()) {
                    if (obj instanceof MessageLayout) {
                        if (((MessageLayout) obj) == messageLayout) {
                            messageContainer.replace((MessageLayout) obj, new Paragraph("oui"));
                        }
                    }
                }
                break;
            case "UPDATE_ALL":
                messageContainer.removeAll();
                for (PublicChatMessage message : getController().getChatMessagesForChannel(textChannel.getId())) {
                    System.out.println("Message" + message.getMessage());
                    messageContainer.add(new MessageLayout(message));
                }
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
        private final int SIZEWIDTH = 40;
        private final int SIZEHEIGHT = 40;

        private final VerticalLayout messageFullWithResponseLayout;
        private final HorizontalLayout messageFullLayout;
        private final VerticalLayout chatUserInformation;
        private final HorizontalLayout optionsUser;
        private final FlexLayout optionMenu;
        private final PopAbsoluteLayout layoutPop;

        private Paragraph metaData;
        private Paragraph message;
        private Image profilPicture;

        private Button response;
        private Button delete;
        private Button modify;

        public MessageLayout(PublicChatMessage publicMessage) {
            this.chatUserInformation = new VerticalLayout();
            setPadding(false);
            setSpacing(false);
            this.layoutPop = new PopAbsoluteLayout();
            this.optionMenu = new FlexLayout();
            this.optionsUser = new HorizontalLayout();
            this.messageFullLayout = new HorizontalLayout();
            this.messageFullWithResponseLayout = new VerticalLayout();

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
            add(messageFullWithResponseLayout);
            add(layoutPop);
        }

        private void createResponseMessage(PublicChatMessage publicMessage) {
            if (publicMessage.getParentId() != 1) {
                PublicChatMessage messageParent = getController().getMessageById(publicMessage.getParentId());
                if (messageParent != null) {
                    Paragraph messageReponse = new Paragraph("Reponse à " + personRepository.findById(messageParent.getSender()).getUsername() + " | "
                            + ((messageParent.getMessage().length() > 50) ? messageParent.getMessage().substring(0, 50) + "..." : messageParent.getMessage()));
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
                    PublicMessagesBroadcaster.broadcast("DELETE_MESSAGE", this);
                    dialog.close();
                    Notification.show("Vous avez supprimé votre message");
                });

                non.addClickListener(ev -> {
                    dialog.close();
                });
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
                        PublicMessagesBroadcaster.broadcast("UPDATE_MESSAGE", this);
                        Notification.show("Vous avez modifié votre message");
                    }
                    dialog.close();
                });

                non.addClickListener(ev -> {
                    dialog.close();
                });
            });
        }

        public void createResponseButton(PublicChatMessage publicMessage) {
            response = new ComponentButton("img/repondre.svg", "Repondre", SIZEWIDTH, SIZEHEIGHT);
            response.addClickListener(ev -> {
                targetResponseMessage = publicMessage.getId();
                messageContainer.getElement().executeJs("this.scrollTop = this.scrollHeight;");
            });
        }

        public void createPopMessage(PublicChatMessage publicMessage) {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Person id = personRepository.findByUsername(authentication.getName());
            optionsUser.add(response);

            //Protection si l'utilisateur est bien le createur du message
            if (id.getId() == publicMessage.getSender()) {
                optionsUser.add(modify);
                optionsUser.add(delete);
            }
            layoutPop.add(optionsUser);
            layoutPop.resize();

            messageFullLayout.add(optionMenu);
        }


        private void createChatBlock(PublicChatMessage publicMessage) {
            this.metaData = createParagrapheAmelioration(getController().getUsernameOfSender(publicMessage) + " | " + convertLongToDate(publicMessage.getTimeCreated()));
            chatUserInformation.add(metaData);
            this.message = createParagrapheAmelioration(publicMessage.getMessage());
            chatUserInformation.add(message);
        }

        private void createPictureSetting() {
            this.profilPicture = new Image("img/Chien 3.jpg", "profilPicture");
            this.profilPicture.setWidth("60px");
            this.profilPicture.setHeight("60px");
            this.profilPicture.getStyle().set("border-radius", "40px");
            this.profilPicture.getStyle().set("margin", "0px 0px 0px 0px");
            this.profilPicture.getStyle().set("padding", "10px");
        }

        private Paragraph createParagrapheAmelioration(String text) {
            Paragraph Data = new Paragraph();
            Data.setText(text);
            Data.getStyle().set("border", "none");
            Data.getStyle().set("border-width", "0px");
            Data.getStyle().set("outline", "none");
            return Data;
        }


        private String convertLongToDate(long dateLong) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat heure = new SimpleDateFormat("HH:mm");
            Date date = new Date(dateLong);
            if (formatter.format(date).equals(formatter.format(new Date(System.currentTimeMillis()))))
                return "Aujourd'hui à " + heure.format(date);
            return formatter.format(date) + " à " + heure.format(date);
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

