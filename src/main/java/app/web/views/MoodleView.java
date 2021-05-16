package app.web.views;

import app.controller.AssignmentController;
import app.controller.ChatController;
import app.controller.Controller;
import app.controller.Markdown;
import app.controller.broadcasters.MoodleBroadcaster;
import app.controller.security.SecurityUtils;
import app.jpa_repo.*;
import app.model.chat.PublicTextChannel;
import app.model.chat.TextChannel;
import app.model.courses.Assignment;
import app.model.courses.Course;
import app.model.courses.MoodlePage;
import app.web.components.UploadComponent;
import app.web.layout.Navbar;
import com.vaadin.component.VaadinClipboard;
import com.vaadin.component.VaadinClipboardImpl;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import com.vaadin.ui.Notification;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Route(value = "moodle", layout = Navbar.class)
public class MoodleView extends ViewWithSidebars implements HasDynamicTitle, HasUrlParameter<Long> {

    private final MoodlePageRepository moodlePageRepository;
    private final CourseRepository courseRepository;

    private final FlexLayout moodleBar = new FlexLayout();

    private Registration broadcasterRegistration;
    private MoodlePage page;

    public MoodleView(@Autowired MoodlePageRepository moodlePageRepository,
                      @Autowired CourseRepository courseRepository,
                      @Autowired PublicTextChannelRepository publicTextChannelRepository,
                      @Autowired PersonRepository personRepository,
                      @Autowired AssignmentRepository assignmentRepository,
                      @Autowired StudentAssignmentsUploadsRepository studentAssignmentsUploadsRepository,
                      @Autowired GroupRepository groupRepository,
                      @Autowired GroupMembersRepository groupMembersRepository,
                      @Autowired PrivateChatMessageRepository privateChatMessageRepository,
                      @Autowired PublicChatMessageRepository publicChatMessageRepository,
                      @Autowired PrivateTextChannelRepository privateTextChannelRepository) {
        this.courseRepository = courseRepository;
        this.moodlePageRepository = moodlePageRepository;
        setPersonRepository(personRepository);
        setController(new Controller(personRepository, publicTextChannelRepository, null,
                                     courseRepository, moodlePageRepository, groupRepository, groupMembersRepository,
                                     privateChatMessageRepository));
        setChatController(new ChatController(personRepository,publicTextChannelRepository,publicChatMessageRepository,
                                    privateTextChannelRepository,privateChatMessageRepository ));
        setAssignmentController(new AssignmentController(personRepository, assignmentRepository,
                studentAssignmentsUploadsRepository, courseRepository));
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        UI ui = attachEvent.getUI();
        broadcasterRegistration = MoodleBroadcaster.register((sectionLayout) -> {
            if (ui.isEnabled() && ui.getUI().isPresent()) {
                ui.access(() -> receiveBroadcast(sectionLayout));
            }
        });
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        broadcasterRegistration.remove();
        broadcasterRegistration = null;
    }

    private void receiveBroadcast(SectionLayout sectionLayout) {
        createMoodleBar();
    }

    /**
     * Creates the center panel that contains the Moodle sections
     */
    public void createMoodleBar() {
        moodleBar.removeAll();
        setCardStyle(moodleBar, "60%", ColorHTML.GREY);
        SectionLayout content = new SectionLayout(page);
        moodleBar.add(content);
    }

    @SneakyThrows // so that javac doesn't complain about not catching the exception
    @Override
    public void setParameter(BeforeEvent event, Long parameter) {
        Optional<MoodlePage> m = moodlePageRepository.findById(parameter);
        if (m.isPresent()) {
            page = m.get();
        } else {
            throw new Exception("There is no course with this ID.");
            // TODO : take care of the exception
        }
        Optional<Course> c = courseRepository.findById(page.getCourseId());
        setCourse(c.orElse(null));

        createSidebar(getCourse().getId());
        createMembersBar(getCourse().getId());
        createMoodleBar();
        createLayout(moodleBar);
    }

    @Override
    public String getPageTitle() {
        return getCourse().getName();
    }

    public enum DialogType {
        LINK, IMAGE;
    }

    /**
     * The Layout that contains for each section :
     * - the title
     * - the content
     * - the delete button
     * - the modify button
     */
    public class SectionLayout extends VerticalLayout implements HasText {
        private final MoodlePage section;

        private final H2 title = new H2();
        private final Paragraph content = new Paragraph();
        private final Dialog modifyPopup = new Dialog();

        public SectionLayout(MoodlePage section) {
            this.section = section;
            if (section == null) return;

            if (!SecurityUtils.isUserStudent()) {
                FlexLayout f = new FlexLayout();

                if (!section.isHomePage())f.add(createDeleteButton());
                f.add(createModifyButton());

                this.add(f);
            }

            initContent();
            createModifyPopup();
        }


        private void initContent() {
            title.add(Markdown.getHtmlFromMarkdown(section.getTitle()));
            content.add(Markdown.getHtmlFromMarkdown(convertIfImagePresent(section.getContent())));
            add(title);
            add(content);
        }

        /**
         * Creates an "<img>" tag with the picture information given in the String
         *
         * @param content a String in the form of : !$ value : height : width!$
         * @return a correctly formatted <img> tag
         */
        private String convertIfImagePresent(String content) {
            String src = StringUtils.substringBetween(content, "!$", "!$");
            while (src != null) {
                String src_original = "!$" + src + "!$";
                String[] tab_source = src.split(":");
                if (tab_source.length < 3) {
                    content = content.replace(src_original, "<img src='moodle/images/" + src + "'>");
                } else {
                    if (tab_source[1].equals("null")) {
                        content = content.replace(src_original, "<img src='moodle/images/" + tab_source[0] + "' height='" + tab_source[2] + "'>");
                    } else if (tab_source[2].equals("null")) {
                        content = content.replace(src_original, "<img src='moodle/images/" + tab_source[0] + "' width='" + tab_source[1] + ">");
                    } else {
                        content = content.replace(src_original, "<img src='moodle/images/" + tab_source[0] + "' width='" + tab_source[1] + "' height='" + tab_source[2] + "'>");
                    }
                }
                src = StringUtils.substringBetween(content, "!$", "!$");
            }
            return content;
        }

        private Button createDeleteButton() {
            Button deleteButton = new Button();
            Image img = new Image("img/corbeille.svg", "edition");
            img.getStyle()
                    .set("width", "25px")
                    .set("margin", "auto");
            deleteButton.setIcon(img);
            deleteButton.addClickListener(event -> {
                getController().deletePage(section);
                UI.getCurrent().navigate("home");
            });
            return deleteButton;
        }

        private Button createModifyButton() {
            Button modifyButton = new Button();
            Image img = new Image("img/editer.svg", "edition");
            img.getStyle()
                    .set("width", "25px")
                    .set("margin", "auto");
            modifyButton.setIcon(img);
            modifyButton.addClickListener(event -> modifyPopup.open());
            return modifyButton;
        }

        private void createModifyPopup() {
            VerticalLayout layout = new VerticalLayout();
            HorizontalLayout layout_horizontal = new HorizontalLayout();
            HorizontalLayout layout_horizontal_button = new HorizontalLayout();

            AtomicReference<DialogMoodle> dialoglink = new AtomicReference<>(new DialogMoodle(modifyPopup, DialogType.LINK));
            AtomicReference<DialogMoodle> dialogimage = new AtomicReference<>(new DialogMoodle(modifyPopup, DialogType.IMAGE));


            Button link = new Button("Ajouter un lien");
            Button image = new Button("Ajouter une Image");
            Label label = new Label("Modifiez la page ici");
            label.getStyle()
                    .set("color",ColorHTML.PURPLE.getColorHtml())
                    .set("font-size", "20px")
                    .set("font-weight","700");
            link.getStyle()
                    .set("background-color",ColorHTML.PURPLE.getColorHtml())
                    .set("color",ColorHTML.WHITE.getColorHtml());
            image.getStyle()
                    .set("background-color",ColorHTML.PURPLE.getColorHtml())
                    .set("color",ColorHTML.WHITE.getColorHtml());
            TextField title = new TextField("Titre");
            title.setValue(section.getTitle());
            TextArea content = new TextArea("Contenu");
            content.setValue(section.getContent());
            Button okButton = new Button("Valider");
            okButton.getStyle()
                    .set("background-color",ColorHTML.PURPLE.getColorHtml())
                    .set("color",ColorHTML.WHITE.getColorHtml());
            okButton.addClickListener(event -> {
                getController().updateSection(section, title.getValue(), content.getValue());
                modifyPopup.close();
                MoodleBroadcaster.broadcast(this);
            });

            link.addClickListener(event -> {
                dialoglink.set(new DialogMoodle(modifyPopup, DialogType.LINK));
                modifyPopup.close();
                dialoglink.get().open();
            });

            image.addClickListener(event -> {
                dialogimage.set(new DialogMoodle(modifyPopup, DialogType.IMAGE));
                modifyPopup.close();
                dialogimage.get().open();
            });

            layout_horizontal_button.add(link, image);
            layout_horizontal.add(okButton);
            layout_horizontal.setPadding(true);
            layout_horizontal.setSpacing(true);
            layout.add(label, title, content, layout_horizontal_button, layout_horizontal);
            layout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
            modifyPopup.add(layout);
        }

    }


    /**
     * A dialog that allows the user to generates valid links, and to upload images
     */
    private class DialogMoodle extends Dialog {

        private final Dialog parent;
        private final Map<Tab, Component> tabsToPages = new HashMap<>();
        private long targetId;

        DialogMoodle(Dialog parent, DialogType type) {
            this.parent = parent;
            if (type == DialogType.LINK) {
                createTab(interneLinkDiv(), externeLinkDiv(), "Lien");
            } else if (type == DialogType.IMAGE) {
                createTab(interneImageDiv(), externeImageDiv(), "Image");
            } else {
                Notification.show("Erreur: Impossible de charger le dialog demandé.");
            }
        }

        private void createTab(Div div_interne, Div div_externe, String name) {
            Tab externe = new Tab(name + " externe");
            tabsToPages.put(externe, div_externe);

            Tab interne = new Tab(name + " interne");
            tabsToPages.put(interne, div_interne);
            div_interne.setVisible(false);

            Tabs tabs = new Tabs(externe, interne);

            tabs.addSelectedChangeListener(event -> {
                tabsToPages.values().forEach(e -> e.setVisible(false));
                tabsToPages.get(tabs.getSelectedTab()).setVisible(true);
            });
            this.addDialogCloseActionListener(event -> {
                parent.open();
                this.close();
            });
            add(tabs, div_externe, div_interne);
        }

        private Div externeLinkDiv() {
            Div d = new Div();

            HorizontalLayout insertLayout = new HorizontalLayout();
            HorizontalLayout buttonLayout = new HorizontalLayout();
            VerticalLayout mainLayout = new VerticalLayout();

            TextField textField = createTextField("Texte à l'affichage: ", "Entre le nom du lien ici...");
            TextField lien = createTextField("Votre lien: ", "Entre votre lien ici....");
            TextArea text = createTextArea("Texte généré:", "");

            Button valide = new Button("Generer");
            Button copie = createCopyButton(textField.getValue());
            Button close = createCloseButton();
            valide.addClickListener(event -> {
                if (textField.isEmpty() || lien.isEmpty()) {
                    text.setValue("Erreur champs invalide");
                } else if (!isLink(lien.getValue())) {
                    text.setValue("Erreur lien non valide");
                } else {
                    text.setValue("[" + textField.getValue() + "](" + lien.getValue() + ")");
                }
            });

            buttonLayout.add(valide, copie, close);
            insertLayout.add(textField, lien);
            mainLayout.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
            mainLayout.add(insertLayout, text, buttonLayout);
            d.add(mainLayout);
            return d;
        }

        /**
         * Makes sure the link is valid.
         * It has to start with "http" or "https" otherwise the program interprets it as an internal link inside the
         * website.
         *
         * @param s the link to consider
         * @return true if the link is valid, false otherwise
         */
        private boolean isLink(String s) {
            return (s.startsWith("http") || s.startsWith("https")) && s.contains(".");
        }

        private Div interneLinkDiv() {
            Div interne = new Div();
            AtomicReference<String> url = new AtomicReference<>("channels");
            List<Assignment> assignment = getAssignmentController().getAssignmentsForCourse(getCourse().getId());
            List<PublicTextChannel> channels = getController().getAllChannelsForCourse(getCourse().getId());
            List<MoodlePage> moodle = getController().getAllMoodlePageForCourse(getCourse().getId());

            Map<Integer, Component> selectMap = new HashMap<>();


            HorizontalLayout insertLayout = new HorizontalLayout();
            HorizontalLayout buttonLayout = new HorizontalLayout();
            VerticalLayout mainLayout = new VerticalLayout();

            TextField msg = createTextField("Texte à l'affichage: ", "Entre le nom du lien ici...");
            TextArea textField = createTextArea("Texte généré:", "");

            Select<Assignment> select_assignment = new Select<>();
            select_assignment.setLabel("Sélectionnez une redirection : ");
            select_assignment.setTextRenderer(Assignment::getName);
            select_assignment.setItems(assignment);

            Select<PublicTextChannel> select_channel = new Select<>();
            select_channel.setLabel("Sélectionnez une redirection : ");
            select_channel.setTextRenderer(TextChannel::getName);
            select_channel.setItems(channels);

            Select<MoodlePage> select_moodle = new Select<>();
            select_moodle.setLabel("Sélectionnez une redirection : ");
            select_moodle.setTextRenderer(MoodlePage::getTitle);
            select_moodle.setItems(moodle);
            //Map
            selectMap.put(0, select_assignment);
            selectMap.put(1, select_channel);
            selectMap.put(2, select_moodle);

            selectMap.values().forEach(e -> e.setVisible(false));
            select_channel.setVisible(true);

            RadioButtonGroup<String> radio = createRadioDefault();

            radio.addValueChangeListener(event -> {

                if (event.getValue().equals("Salon de discussion")) {
                    selectMap.values().forEach(e -> e.setVisible(false));
                    select_channel.setVisible(true);
                    url.set("channels");
                } else if (event.getValue().equals("Devoir à rendre")) {
                    selectMap.values().forEach(e -> e.setVisible(false));
                    select_assignment.setVisible(true);
                    url.set("assignment");
                } else {
                    selectMap.values().forEach(e -> e.setVisible(false));
                    select_moodle.setVisible(true);
                    url.set("moodle");
                }
            });

            Button valide = new Button("Generer");
            Button copie = createCopyButton(textField.getValue());
            Button close = createCloseButton();
            valide.addClickListener(event -> {
                if (msg.isEmpty()) {
                    textField.setValue("Erreur champs invalide");
                } else {
                    String urlRadicale = "http://localhost:8080/";

                    switch (url.get()) {
                        case "channels":
                            if (select_channel.getValue() == null) {
                                textField.setValue("Erreur : aucune sélection");
                            } else {
                                targetId = select_channel.getValue().getId();
                                textField.setValue("[" + msg.getValue() + "](" + urlRadicale + url + "/" + targetId + " )");
                            }
                            break;
                        case "assignment":
                            if (select_assignment.getValue() == null) {
                                textField.setValue("Erreur : aucune sélection");
                            } else {
                                targetId = select_assignment.getValue().getId();
                                textField.setValue("[" + msg.getValue() + "](" + urlRadicale + url + "/" + targetId + " )");
                            }
                            break;
                        case "moodle":
                            if (select_moodle.getValue() == null) {
                                textField.setValue("Erreur : aucune sélection");
                            } else {
                                targetId = select_moodle.getValue().getId();
                                textField.setValue("[" + msg.getValue() + "](" + urlRadicale + url + "/" + targetId + " )");
                            }
                            break;
                    }
                }
            });

            insertLayout.add(msg, radio, select_assignment, select_channel, select_moodle);
            buttonLayout.add(valide, copie, close);
            mainLayout.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
            mainLayout.add(insertLayout, textField, buttonLayout);
            interne.add(mainLayout);
            return interne;
        }

        private Div externeImageDiv() {
            Div d = new Div();

            HorizontalLayout insertLayout = new HorizontalLayout();
            HorizontalLayout buttonLayout = new HorizontalLayout();
            VerticalLayout mainLayout = new VerticalLayout();

            TextField lien = createTextField("Votre lien: ", "Entre votre lien ici....");
            TextArea text = createTextArea("Texte généré:", "");
            Paragraph warning = new Paragraph("Les liens autorisés doivent finir par : .png, .jpg ou .jpeg");

            Button valide = new Button("Générer");
            Button copie = new Button("Copier");
            Button close = createCloseButton();
            valide.addClickListener(event -> {
                if (lien.isEmpty()) {
                    text.setValue("Erreur : champs invalides");
                } else if (!isLink(lien.getValue())) {
                    text.setValue("Erreur : lien non valide");
                } else if (!isImage(lien.getValue())) {
                    text.setValue("Erreur : ce lien n'est pas une image");
                } else {
                    text.setValue("![](" + lien.getValue() + ")");
                }
            });

            copie.addClickListener(event -> copyInClipBoard(text.getValue()));
            buttonLayout.add(valide, copie, close);
            insertLayout.add(lien);
            mainLayout.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
            mainLayout.add(warning, insertLayout, text, buttonLayout);
            d.add(mainLayout);
            return d;
        }

        private boolean isImage(String s) {
            return (s.toLowerCase().endsWith("jpg") || s.toLowerCase().endsWith("png") || s.toLowerCase().endsWith("jpeg")) && s.contains(".");
        }

        private Button createCloseButton() {
            Button close = new Button("Fermer");
            close.addClickListener(event -> {
                this.close();
                this.parent.open();
            });
            return close;
        }

        private Button createCopyButton(String value) {
            Button copy = new Button("Copier");
            copy.addClickListener(event -> copyInClipBoard(value));
            return copy;
        }

        private void copyInClipBoard(String text) {
            //il faut que ce soit en https
            VaadinClipboard vaadinClipboard = VaadinClipboardImpl.GetInstance();
            vaadinClipboard.copyToClipboard(text, copySuccess -> {
                if (copySuccess) {
                    Notification.show("'" + text + "'" + " a bien été copié");
                } else {
                    Notification.show("Erreur : la copie n'a pas été effectuée.", Notification.Type.ERROR_MESSAGE);
                }
            });
        }

        private Div interneImageDiv() {
            Div interne = new Div();

            String newDirName = "src/main/webapp/moodle/images/" + getCourse().getId();

            UploadComponent uploadComponent = new UploadComponent("100%", "100%", 1, 5242880, newDirName, ".jpg", ".jpeg", ".png");
            uploadComponent.setDropLabel(new Paragraph("Sélectionnez votre fichier (5MO max)"));
            String nameChiffre = getRandomId() + "_" + getCourse().getId();
            HorizontalLayout insertLayout = new HorizontalLayout();
            HorizontalLayout buttonLayout = new HorizontalLayout();
            VerticalLayout mainLayout = new VerticalLayout();

            TextArea text = createTextArea("Texte généré:", "");
            Paragraph warning = new Paragraph("Les fichiers autorisés sont .png, .jpg, .jpeg " +
                                                      "\n Votre fichier doit faire moins de 5Mo");

            Div redimension = new Div();
            Paragraph p = new Paragraph("Redimensionnez votre image (avant Upload) : ");
            HorizontalLayout layout = new HorizontalLayout();
            NumberField height = new NumberField("");
            height.setLabel("Hauteur (px)");
            height.setMax(1080);
            NumberField width = new NumberField("");
            width.setLabel("Largeur (px)");
            width.setMax(1920);
            layout.add(width, height);
            redimension.add(p, layout);

            Button copie = new Button("Copier");
            Button close = createCloseButton();

            uploadComponent.addSucceededListener(event -> {
                String newPath = "";
                com.vaadin.flow.component.notification.Notification.show("Votre fichier est bien téléchargé.");
                try {
                    newPath = renameFile(uploadComponent.getFileName(), newDirName, nameChiffre);
                } catch (Exception e) {
                    text.setValue("Une erreur est survenue, le fichier n'existe pas.");
                    close();
                    this.parent.open();
                }

                if (imageExist(newPath)) {
                    if (height.isEmpty() && width.isEmpty()) {
                        text.setValue("!$" + newPath.replace("src/main/webapp/moodle/images/", "") + "!$");
                    } else {
                        if (!width.isEmpty() && !height.isEmpty()) {
                            text.setValue("!$" + newPath.replace("src/main/webapp/moodle/images/", "") + ":" + width.getValue().intValue() + ":" + height.getValue().intValue() + "!$");
                        }
                        if (width.isEmpty() && !height.isEmpty()) {
                            text.setValue("!$" + newPath.replace("src/main/webapp/moodle/images/", "") + ":" + width.getValue() + ":" + height.getValue().intValue() + "!$");
                        }
                        if (!width.isEmpty() && height.isEmpty()) {
                            text.setValue("!$" + newPath.replace("src/main/webapp/moodle/images/", "") + ":" + width.getValue().intValue() + ":" + height.getValue() + "!$");
                        }
                    }
                    uploadComponent.setDropAllowed(false);
                } else {
                    text.setValue("Une erreur est survenue, le fichier n'existe pas.");
                }
            });

            uploadComponent.addFileRejectedListener(event -> {
                com.vaadin.flow.component.notification.Notification.show("Enregistrement de l'image impossible");
                com.vaadin.flow.component.notification.Notification.show(event.getErrorMessage());
            });

            insertLayout.add(uploadComponent);
            buttonLayout.add(copie, close);
            mainLayout.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
            mainLayout.add(warning, redimension, insertLayout, text, buttonLayout);
            interne.add(mainLayout);
            return interne;
        }

        private TextField createTextField(String label, String placeHolder) {
            TextField textField = new TextField();
            textField.setLabel(label);
            textField.setPlaceholder(placeHolder);
            return textField;
        }

        private TextArea createTextArea(String label, String placeHolder) {
            TextArea textArea = new TextArea();
            textArea.setLabel(label);
            textArea.setPlaceholder(placeHolder);
            textArea.setReadOnly(true);
            textArea.getStyle().set("margin", "auto").set("width", "100%");
            return textArea;
        }

        private RadioButtonGroup<String> createRadioDefault() {
            RadioButtonGroup<String> radio = new RadioButtonGroup<>();
            radio.setItems("Salon de discussion", "Devoir à rendre", "Moodle présentation");
            radio.setLabel("Sélectionnez le type de redirection: ");
            radio.setValue("Salon de discussion");
            radio.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
            return radio;
        }

        private String getExtensionImage(String name) {
            String[] tab_name = name.toLowerCase().split("\\.");
            if (tab_name.length > 2) return "";
            if (tab_name[1].contains("jpg") || tab_name[1].contains("jpeg") || tab_name[1].contains("png")) {
                return tab_name[1];
            }
            return "";
        }

        private String renameFile(String oldName, String path, String linkName) throws Exception {
            String extension = getExtensionImage(oldName);
            String newPath = path + "/" + linkName + "." + extension;
            File old = new File(oldName);
            File newFile = new File(newPath);
            if (!old.renameTo(newFile)) {
                throw new Exception("File can't be renamed");
            }
            return newPath;
        }

        private boolean imageExist(String url) {
            File f = new File(url);
            return f.exists();
        }

        private long getRandomId() {
            return new Random().nextLong();
        }
    }

}