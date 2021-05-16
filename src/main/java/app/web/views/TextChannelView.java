package app.web.views;

import app.controller.*;
import app.controller.broadcasters.ChatMessagesBroadcaster;
import app.controller.commands.CommandsClearChat;
import app.controller.security.SecurityUtils;
import app.jpa_repo.*;
import app.model.chat.*;
import app.model.courses.Course;
import app.model.users.Person;
import app.web.components.ComponentButton;
import app.web.components.UploadComponent;
import app.web.layout.Navbar;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import lombok.Getter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


@Route(value = "channels", layout = Navbar.class)
public class TextChannelView extends ViewWithSidebars implements HasDynamicTitle {

    protected final PersonRepository personRepository;
    protected final CourseRepository courseRepository;
    protected final Person currentUser;
    protected final ChatController chatController;
    protected final TextField messageTextField;
    protected final FlexLayout chatBar = new FlexLayout();
    protected final FlexLayout messageContainer = new FlexLayout();
    protected TextChannel textChannel;
    protected long targetResponseMessage;
    protected Registration broadcasterRegistration;
    protected MessageResponsePopComponent selectRep;
    protected Button exitButton;
    protected Button sendMessage;
    protected ComponentButton muteMicrophone;
    protected ComponentButton muteHeadphone;
    protected VerticalLayout layoutMaster = new VerticalLayout();
    protected HorizontalLayout messageInputBar = new HorizontalLayout();

    public TextChannelView(PublicTextChannelRepository publicTextChannelRepository,
                           PublicChatMessageRepository publicChatMessageRepository,
                           PrivateTextChannelRepository privateTextChannelRepository,
                           PrivateChatMessageRepository privateChatMessageRepository,
                           PersonRepository personRepository,
                           AssignmentRepository assignmentRepository,
                           CourseRepository courseRepository,
                           StudentAssignmentsUploadsRepository studentAssignmentsUploadsRepository,
                           GroupRepository groupRepository,
                           GroupMembersRepository groupMembersRepository,
                           MoodlePageRepository moodlePageRepository) {
        this.personRepository = personRepository;
        this.courseRepository = courseRepository;
        setPersonRepository(personRepository);

        // by default, the user is not responding to any message
        this.targetResponseMessage = 0;

        // set the user and the controllers
        currentUser = SecurityUtils.getCurrentUser(personRepository);
        setController(new Controller(personRepository, publicTextChannelRepository, publicChatMessageRepository,
                                     courseRepository, moodlePageRepository, groupRepository, groupMembersRepository,
                                     privateChatMessageRepository));
        setChatController(new ChatController(personRepository,publicTextChannelRepository,publicChatMessageRepository,
                privateTextChannelRepository,privateChatMessageRepository ));
        setAssignmentController(new AssignmentController(personRepository, assignmentRepository,
                                                         studentAssignmentsUploadsRepository, courseRepository));
        this.chatController = new ChatController(personRepository, publicTextChannelRepository, publicChatMessageRepository,
                                                 privateTextChannelRepository, privateChatMessageRepository);

        this.messageTextField = createTextField();
        createVoiceChatButtons();
        createSendMessageButton();
        scrollDownChat(); // so that the last messages are visible when loading the page
    }

    private void createSendMessageButton() {
        sendMessage = createButtonWithLabel("Envoyer", ColorHTML.PURPLE.getColorHtml());
        sendMessage.addClickShortcut(Key.ENTER);

        sendMessage.addClickListener(event -> {
            if (!messageTextField.isEmpty()) {
                if (!messageTextField.getValue().startsWith("/")) {
                    ChatMessage newMessage;
                    if (targetResponseMessage == 0) {
                        newMessage = chatController.saveMessage(messageTextField.getValue(), textChannel.getId(),
                                                                1, currentUser.getId(),
                                                                textChannel instanceof PrivateTextChannel,
                                                                0);
                    } else {
                        newMessage = chatController.saveMessage(messageTextField.getValue(), textChannel.getId(),
                                                                targetResponseMessage, currentUser.getId(),
                                                                textChannel instanceof PrivateTextChannel, 0);
                        if (newMessage != null) {
                            selectRep.changeStatus();
                            targetResponseMessage = 0;
                        }
                    }
                    ChatMessagesBroadcaster.broadcast("NEW_MESSAGE", newMessage);
                    scrollDownChat();

                } else {
                    String[] arg = messageTextField.getValue().split(" ");
                    switch (arg[0]) {
                        case "/clear":
                            new CommandsClearChat(chatController, currentUser.getId(), textChannel.getId(), arg);
                            break;
                    }
                    Notification.show("Vous exécutez une commande");
                }
                messageTextField.clear();
                messageTextField.focus();
            }
        });
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        UI ui = attachEvent.getUI();
        broadcasterRegistration = ChatMessagesBroadcaster.register((type, chatMessage) -> {
            if (ui.isEnabled() && ui.getUI().isPresent()) {
                ui.access(() -> receiveBroadcast(type, chatMessage));
            }
        });
    }

    private void receiveBroadcast(String type, ChatMessage message) {
        switch (type) {
            case "NEW_MESSAGE":
                UI.getCurrent().access(() -> messageContainer.add(new MessageLayout(message)));
                break;
            case "DELETE_MESSAGE":
                UI.getCurrent().access(() -> {
                    for (Object obj : messageContainer.getChildren().toArray()) {
                        if (obj instanceof MessageLayout
                                && ((MessageLayout) obj).getChatMessage().getId() == message.getId()) {
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
                                && ((MessageLayout) obj).getChatMessage().getId() == message.getId()) {
                            // we found the layout to update
                            MessageLayout toChange = (MessageLayout) obj;
                            toChange.chatMessage = chatController.findMessageById(message);
                            toChange.messageParagraph.removeAll();
                            toChange.messageParagraph.add(Markdown.getHtmlFromMarkdown(toChange.chatMessage.getMessage()));
                        }
                    }
                });
                break;
            case "UPDATE_ALL":
                UI.getCurrent().access(() -> {
                    messageContainer.removeAll();
                    for (ChatMessage chatMessage : chatController.getAllChatMessagesForChannel(textChannel)) {
                        messageContainer.add(new MessageLayout(chatMessage));
                    }
                });
                break;
            default:
                break;
        }
    }

    @Override
    public String getPageTitle() {
        if (textChannel != null) {
            return textChannel.getName();
        } else {
            return "Homepage";
        }
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
        chatButtonContainer.add(sendMessage/*, muteMicrophone, muteHeadphone, exitButton*/);
        Button addFileOrImage = createButtonOpenDialogUpload();

        messageInputBar.add(addFileOrImage, messageTextField, chatButtonContainer);

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

        layoutMaster.add(messageInputBar);
        for (ChatMessage message : chatController.getAllChatMessagesForChannel(textChannel)) {
            if (!message.isDeleted()) messageContainer.add(new MessageLayout(message));
        }
        chatBar.add(messageContainer, layoutMaster);
    }

    private Div createUploadDialog(String title, String subTitle, UploadComponent component) {
        final Div element = new Div();
        final H1 h1 = new H1(title);
        final Paragraph p = new Paragraph(subTitle);
        final VerticalLayout layout = new VerticalLayout();
        h1.getStyle().set("color", ColorHTML.PURPLE.getColorHtml());
        layout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        layout.add(h1, p, component);
        element.add(layout);
        return element;
    }

    private Tabs createTabElement(String tabName, String tabName2,Div image, Div file){
        final Tab tab = new Tab(tabName);
        final Tab tab2 = new Tab(tabName2);
        final Tabs tabs = new Tabs(tab, tab2);
        Map<Tab, Div> tabsToPages = new HashMap<>();
        tabsToPages.put(tab, image);
        tabsToPages.put(tab2, file);
        tabs.addSelectedChangeListener(e -> {
            tabsToPages.values().forEach(page -> page.setVisible(false));
            Component selectedPage = tabsToPages.get(tabs.getSelectedTab());
            selectedPage.setVisible(true);
        });
        return tabs;
    }

    private Dialog createDialogUpload() {
        final Dialog dialogMain = new Dialog();
        final UploadComponent fileUpload = uploadElements(dialogMain, 2, "src/main/webapp/userFileChat/files", 500000, ".pdf", ".PDF", ".txt", ".TXT", ".docs");
        final UploadComponent imageUpload = uploadElements(dialogMain, 1, "src/main/webapp/userFileChat/images", 500000, ".jpeg", ".jpg", ".png", ".JPG", ".JPEG");
        final Div image = createUploadDialog("Télécharger une nouvelle image\n", "Choisissez une nouvelle image depuis votre navigateur (ou faites un glisser-déposer). Seuls les fichiers .jpg et .jpeg sont acceptés.", imageUpload);
        final Div file = createUploadDialog("Télécharger un nouveau fichier\n", "Choisissez un nouveau fichier depuis votre navigateur (ou faites un glisser-déposer). Seuls les fichiers de moins de 6MO et .pdf .txt et .docs sont acceptés.", fileUpload);
        file.setVisible(false);
        Tabs tabs = createTabElement("Image", "Fichier", image, file);
        dialogMain.add(tabs, image, file);
        return dialogMain;
    }


    private UploadComponent uploadElements(Dialog courant, int type, String sources, int limit, String... extension) {
        final UploadComponent component = new UploadComponent("500", "500", 1, limit,
                sources, extension);
        component.addSucceededListener(event -> {
            String oldName = component.getFileName();
            String newName = "";
            try {
                newName = renameFile(oldName, sources);
            } catch (Exception e) {
                e.printStackTrace();
            }
            courant.close();
            Dialog successUpload = successUploadDialog(sources, newName, type);
            successUpload.open();
        });

        component.addFileRejectedListener(event -> {
            courant.close();
            Dialog errorDialog = errorUploadDialog("Votre fichier ne respecte pas les conditions d'envoi !");
            errorDialog.open();
        });

        component.addFailedListener(event -> {
            courant.close();
            Dialog errorDialog = errorUploadDialog("Votre fichier n'as pas atteint la bonne destination... Réessayez !");
            errorDialog.open();
        });
        return component;
    }

    private Dialog successUploadDialog(String sources, String nameFile, int type) {
        final Dialog dialogSuccess = new Dialog();
        final H1 h1 = new H1("Fichier télécharger avec succès ");
        final Paragraph p = new Paragraph("Votre fichier est sur le point d'etre envoyé !");
        final Button send = new Button("Envoyer");
        final Button close = new Button("Fermer");
        final VerticalLayout layout = new VerticalLayout();
        final HorizontalLayout buttonLayout = new HorizontalLayout();


        close.addClickListener(event -> {
            dialogSuccess.close();
            deleteFile(new File(sources + "/" + nameFile));
        });

        dialogSuccess.addDialogCloseActionListener(event -> {
            if (event.getSource().isCloseOnOutsideClick() && event.getSource().isCloseOnEsc()) {
                deleteFile(new File(sources + "/" + nameFile));
            }
        });

        send.addClickListener(event -> {
            dialogSuccess.close();
            sendMessageChat(type, sources + "/" + nameFile);
            scrollDownChat();
        });


        buttonLayout.add(close, send);
        h1.getStyle().set("color", "#32CD32");
        layout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        layout.add(h1, p, buttonLayout);
        dialogSuccess.add(layout);
        return dialogSuccess;
    }

    private Dialog errorUploadDialog(String subtitle) {
        final Dialog dialogError = new Dialog();
        final H1 h1 = new H1("Erreur d'envoi de votre fichier");
        final Paragraph p = new Paragraph(subtitle);
        final Button close = new Button("Fermer");
        close.addClickListener(event -> dialogError.close());
        final VerticalLayout layout = new VerticalLayout();
        h1.getStyle().set("color", "#FF0000");
        layout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        layout.add(h1, p, close);
        dialogError.add(layout);
        return dialogError;
    }

    private Button createButtonOpenDialogUpload() {
        final Icon a = new Icon(VaadinIcon.PLUS_CIRCLE);
        a.setColor(ColorHTML.PURPLE.getColorHtml());
        Button addFileOrImage = new Button("", a);
        addFileOrImage.addClickListener(event -> {
            Dialog d = createDialogUpload();
            d.open();
        });
        return addFileOrImage;
    }

    private String renameFile(String oldName, String sourceFichier) throws Exception {
        final String extension = getExtension(oldName);
        File old = new File(oldName);
        final String newNameFile = currentUser.getId() + "_" + textChannel.getId() + "_" + randomId() + "." + extension.toLowerCase();
        File newFile = new File(sourceFichier + "/" + newNameFile);
        if (!old.renameTo(newFile)) {
            throw new Exception("File can't be renamed");
        }
        return newNameFile;
    }

    private void deleteFile(File file) {
        if (file.exists()) {
            file.delete();
        }
    }

    public void sendMessageChat(int type, String source) {
        ChatMessage chatMessage = chatController.saveMessage(source, textChannel.getId(), 1, currentUser.getId(),
                                                             textChannel instanceof PrivateTextChannel, type);
        ChatMessagesBroadcaster.broadcast("NEW_MESSAGE", chatMessage);
    }

    private long randomId() {
        Random r = new Random();
        return r.nextLong();
    }

    private String getExtension(String name) {
        String[] tab_name = name.toLowerCase().split("\\.");
        if (tab_name.length > 2) return "";
        return tab_name[1];
    }

    public class MessageResponsePopComponent extends Div {
        VerticalLayout layoutVerticalLayout;

        Paragraph message;
        Button annuler;

        public MessageResponsePopComponent() {
            annuler = new Button("", new Icon(VaadinIcon.CLOSE_SMALL));
            message = new Paragraph("");
            message.getStyle()
                    .set("margin","auto auto auto 12px")
                    .set("padding","0")
                    .set("margin-left","0")
                    .set("float","left")
                    .set("left","0")
                    .set("color",ColorHTML.PURPLE.getColorHtml());
            annuler.getStyle()
                    .set("margin","auto 0px auto auto")
                    .set("padding","0")
                    .set("margin-right","0")
                    .set("float","right")
                    .set("right","0")
                    .set("color",ColorHTML.PURPLE.getColorHtml());
            layoutVerticalLayout = new VerticalLayout();
            getStyle().set("background-color", ColorHTML.DARKGREY.getColorHtml());
            setVisible(false);
            setStyle();
            eventClickMessage();
            layoutVerticalLayout.add(message, annuler);
            layoutVerticalLayout.getStyle()
                    .set("flex-direction","row")
                    .set("padding","5px");
            add(layoutVerticalLayout);
        }

        public void setStyle() {
            this.getStyle()
                    .set("flex-direction","row")
                    .set("display","flex")
                    .set("border-radius","4px");
        }

        public void setMessage(String userTo) {
            message.setText("Répondre à " + userTo);
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

    public class MessageLayout extends HorizontalLayout {
        private final int SIZEWIDTH = 25;
        private final int SIZEHEIGHT = 15;

        @Getter
        private ChatMessage chatMessage;

        private final Div messageFullWithResponseLayout;
        private final HorizontalLayout messageFullLayout;
        private final VerticalLayout chatUserInformation;
        private final HorizontalLayout optionsUser;
        private final FlexLayout optionMenu;
        private final PopAbsoluteLayout layoutPop;

        private Paragraph messageParagraph;
        private Image profilPicture;

        private Button response;
        private Button delete;
        private Button modify;
        private Button report;


        public MessageLayout(ChatMessage chatMessage) {
            this.chatMessage = chatMessage;
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
            createResponseMessage();
            createPictureSetting(chatMessage.getSender());
            createDeleteButton();
            createModifyButton();
            createReportButton();
            createResponseButton();

            createPopMessage();
            createChatBlock();
            messageFullLayout.add(profilPicture);
            messageFullLayout.add(chatUserInformation);
            messageFullWithResponseLayout.add(messageFullLayout);
            messageFullWithResponseLayout.getStyle()
                    .set("padding", "2.5px");
            add(messageFullWithResponseLayout);
            add(layoutPop);
        }

        private void createResponseMessage() {
            if (chatMessage.getParentId() != 1) {
                ChatMessage messageParent = chatController.getMessageById(chatMessage.getParentId(), chatMessage instanceof PrivateChatMessage);
                if (messageParent != null) {
                    Paragraph answerInfo;
                    if (!messageParent.isDeleted()) {
                        if (messageParent.getType() == 0) {
                            answerInfo = new Paragraph("Réponse à " + personRepository.findById(messageParent.getSender()).getUsername() + " | "
                                    + ((messageParent.getMessage().length() > 50) ? messageParent.getMessage().substring(0, 50) + "..." : messageParent.getMessage()));
                        } else if (messageParent.getType() == 1) {
                            answerInfo = new Paragraph("Réponse à " + personRepository.findById(messageParent.getSender()).getUsername() + " | Image de l'utilisateur");
                        } else {
                            answerInfo = new Paragraph("Réponse à " + personRepository.findById(messageParent.getSender()).getUsername() + " | Fichier de l'utilisateur");
                        }
                    } else {
                        answerInfo = new Paragraph("Message supprimé par l'utilisateur");
                    }
                    messageFullWithResponseLayout.add(answerInfo);
                }
            }
        }

        private void createDeleteButton() {
            delete = new ComponentButton("img/corbeille.svg", "Supprimer", SIZEWIDTH, SIZEHEIGHT);

            delete.addClickListener(event -> {
                Dialog dialog = new Dialog();
                dialog.add(new Paragraph("Voulez-vous vraiment supprimer votre message ?"));
                Button oui = new Button("Oui");
                Button non = new Button("Non");

                dialog.add(oui);
                dialog.add(non);
                dialog.open();

                oui.addClickListener(ev -> {
                    chatController.deleteMessage(chatMessage);
                    ChatMessagesBroadcaster.broadcast("DELETE_MESSAGE", chatMessage);
                    dialog.close();
                    Notification.show("Vous avez supprimé votre message");
                });
                non.addClickListener(ev -> dialog.close());
            });
        }

        private void createModifyButton() {
            modify = new ComponentButton("img/editer.svg", "Editer", SIZEWIDTH, SIZEHEIGHT);

            modify.addClickListener(event -> {
                Dialog dialog = new Dialog();

                Button oui = new Button("Valider");
                Button non = new Button("Annuler");

                TextField messageUpdate = new TextField();
                messageUpdate.setValue(chatMessage.getMessage());
                dialog.add(new Paragraph("Voulez-vous modifier votre message ?"));
                dialog.add(messageUpdate);

                dialog.add(oui);
                dialog.add(non);
                dialog.open();

                oui.addClickListener(ev -> {
                    if (!messageUpdate.getValue().equals(chatMessage.getMessage()) && !messageUpdate.isEmpty()) {
                        chatController.changeMessage(chatMessage, messageUpdate.getValue());
                        ChatMessagesBroadcaster.broadcast("UPDATE_MESSAGE", chatMessage);
                        Notification.show("Vous avez modifié votre message");
                    }
                    dialog.close();
                });

                non.addClickListener(ev -> dialog.close());
            });
        }

        public void createResponseButton() {
            response = new ComponentButton("img/repondre.svg", "Repondre", SIZEWIDTH, SIZEHEIGHT);
            response.addClickListener(ev -> {
                if (chatMessage != null) {
                    targetResponseMessage = chatMessage.getId();
                    selectRep.setMessage(personRepository.findById(chatMessage.getSender()).getUsername());
                    if (!selectRep.isVisible()) selectRep.show();
                    scrollDownChat();
                }
            });
        }

        private void createReportButton() {
            report = new ComponentButton(VaadinIcon.EXCLAMATION, SIZEWIDTH*2, SIZEHEIGHT*2);
            report.getStyle().set("color", ColorHTML.PURPLE.getColorHtml());
            report.addClickListener(event -> {
                Dialog dialog = new Dialog();
                Div buttons = new Div();
                Button valider = new Button("Valider");
                Button fermer = new Button("fermer");
                H1 title = new H1("Signaler un utilisateur");
                ComboBox<String> reasonReport = new ComboBox<>();
                TextField reasonTextfield = new TextField();
                reportDialogStyle(dialog, buttons, valider, fermer, title, reasonReport, reasonTextfield);

                reasonReport.addValueChangeListener(evt -> {
                    if (evt.getValue().equals("Autre...")) {
                        reasonTextfield.setVisible(true);
                        reasonTextfield.setPlaceholder("Décrivez votre raison");
                    }
                });
                fermer.addClickListener(e -> dialog.close());

                valider.addClickListener(evt -> {
                    sendReport(reasonReport, reasonTextfield);
                    dialog.close();
                });
                dialog.add(title, reasonReport, reasonTextfield, buttons);
                dialog.open();
            });
        }

        private void reportDialogStyle(Dialog dialog, Div buttons, Button valider, Button fermer, H1 title, ComboBox<String> reasonReport, TextField reasonTextfield) {
            dialog.setHeight("40%");

            fermer.getStyle()
                    .set("background-color", "#F04747")
                    .set("color", "white");

            valider.getStyle()
                    .set("background-color", ColorHTML.PURPLE.getColorHtml())
                    .set("color","white");

            title.getStyle().set("color",ColorHTML.PURPLE.getColorHtml());

            reasonTextfield.setVisible(false);
            reasonTextfield.getStyle()
                    .set("display","block")
                    .set("margin-top","-30px");

            buttons.add(valider,fermer);
            buttons.getStyle()
                    .set("display", "inline")
                    .set("width", "50%")
                    .set("padding", "10px")
                    .set("margin-top", "15px")
                    .set("margin-left", "25%");

            fermer.getStyle().set("margin-left", "10px");

            reasonReport.getStyle().set("display", "block");
            reasonReport.setItems("Propos désagréables", "Mauvais comportement", "Autre...");
            reasonReport.setPlaceholder("Raison du signalement");
            reasonReport.isRequired();
        }

        private void sendReport(ComboBox<String> reasonReport, TextField reasonTextfield) {
            long ok = chatController.createNewPrivateChannel(currentUser.getId(), "pseudo", "admin");
            PublicTextChannel publicTextChannel = getController().getTextChannel(textChannel.getId());
            Course course = getController().findCourseById(publicTextChannel.getCourseId());
            if (reasonReport.getValue().equals("Autre...")) {
                String message = "Je signale l'user @" + chatController.getUsernameOfSender(chatMessage) + " pour cause : "
                        + reasonTextfield.getValue() + ", dans le channel :" + textChannel.getName() + ". Dans le cours :"
                        + course.getName();
                chatController.saveMessage(message, ok, chatMessage.getParentId(), currentUser.getId(), true, 0);
            } else {
                String message = "Je signale l'user @" + chatController.getUsernameOfSender(chatMessage) + " pour motif : "
                        + reasonReport.getValue() + ", dans le channel :" + textChannel.getName() + ". Dans le cours :"
                        + course.getName();
                chatController.saveMessage(message, ok, chatMessage.getParentId(), currentUser.getId(), true, 0);
            }
            chatController.saveMessage("Votre demande à été transmise avec succès !", ok,
                                       chatMessage.getParentId(), 1, true, 0);
            if (ok == -1) {
                Notification.show("Votre signalement n'a pas pu être effectué.");
            } else {
                String notificationMessage = "Signalement de l'utilisateur @" +
                        chatController.getUsernameOfSender(chatMessage) + " réussi !";
                Notification.show(notificationMessage).setPosition(Notification.Position.MIDDLE);
            }
        }

        public void createPopMessage() {
            optionsUser.add(response);

            if (currentUser.getId() == chatMessage.getSender()) {
                optionsUser.add(modify);
                optionsUser.add(delete);
            }
            if(currentUser.getId() != chatMessage.getSender() && !getController().findUserById(chatMessage.getSender()).getRoleAsString().equals("ADMIN")){
                optionsUser.add(report);
            }
            layoutPop.add(optionsUser);
            layoutPop.resize();

            messageFullLayout.add(optionMenu);
        }

        private void createChatBlock() {
            Paragraph metaData = createParagrapheAmelioration(chatController.getUsernameOfSender(chatMessage) + " | "
                                                                      + getController().convertLongToDate(chatMessage.getTimeCreated()));
            final Paragraph error = new Paragraph("Erreur : fichier introuvable");
            metaData.getStyle()
                    .set("color", ColorHTML.PURPLE.getColorHtml())
                    .set("font-weight", "700");
            chatUserInformation.add(metaData);
            if (chatMessage.getType() == 0) {
                this.messageParagraph = createParagrapheAmelioration(chatMessage.getMessage());
                chatUserInformation.add(messageParagraph);
            } else if (chatMessage.getType() == 1) {
                if (chatMessage.getMessage().length() > 0) {
                    File f = new File(chatMessage.getMessage());
                    if (f.exists()) {
                        final Image image = createImageChat(chatMessage);
                        chatUserInformation.setHeight("70%");
                        chatUserInformation.add(image);
                    } else {
                        chatUserInformation.add(error);
                    }
                } else {
                    chatUserInformation.add(error);
                }
            } else if (chatMessage.getType() == 2) {
                if (chatMessage.getMessage().length() > 0) {
                    final DownloadController downloadController = createDownloadButton(chatMessage.getMessage(), "");
                    chatUserInformation.add(downloadController);
                } else {
                    chatUserInformation.add(error);
                }
            }
        }

        public Image createImageChat(ChatMessage chatMessage) {
            Image image = new Image(chatMessage.getMessage().replace("src/main/webapp/", ""), "imageChat");
            image.setWidth("70%");
            image.setHeight("70%");
            return image;
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

        private DownloadController createDownloadButton(String outputName, String sourceDir) {
            String nameFile = outputName.split("/")[outputName.split("/").length - 1];
            return new DownloadController(nameFile + " | Télécharger", nameFile,
                    outputStream -> {
                        try {
                            File file = new File(outputName);
                            byte[] toWrite = FileUtils.readFileToByteArray(file);
                            outputStream.write(toWrite);
                        } catch (IOException e) {
                            System.out.println("Problem while writing the file");
                            e.printStackTrace();
                        }
                    });
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
                setWidth(SIZEWIDTH * this.getElement().getChildren().findAny().get().getChildCount() + 20 + "px");
                setHeight((SIZEHEIGHT + 2) + "px");
                setVisible(false);
            }

        }

    }
}

