package app.web.views;

import app.controller.AssignmentController;
import app.controller.Controller;
import app.controller.Markdown;
import app.controller.MoodleBroadcaster;
import app.controller.security.SecurityUtils;
import app.jpa_repo.*;
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

    private final PersonRepository personRepository;
    private final MoodlePageRepository moodlePageRepository;
    private final CourseRepository courseRepository;
    private Course course;

    private final FlexLayout moodleBar = new FlexLayout();

    private Registration broadcasterRegistration;
    private MoodlePage page;

    public MoodleView(@Autowired MoodlePageRepository moodlePageRepository,
                      @Autowired CourseRepository courseRepository,
                      @Autowired TextChannelRepository textChannelRepository,
                      @Autowired PersonRepository personRepository,
                      @Autowired AssignmentRepository assignmentRepository,
                      @Autowired StudentAssignmentsUploadsRepository studentAssignmentsUploadsRepository,
                      @Autowired GroupRepository groupRepository,
                      @Autowired GroupMembersRepository groupMembersRepository) {
        this.personRepository = personRepository;
        this.courseRepository = courseRepository;
        this.moodlePageRepository = moodlePageRepository;
        setPersonRepository(personRepository);
        setController(new Controller(personRepository, textChannelRepository, null,
                                     courseRepository, moodlePageRepository, groupRepository, groupMembersRepository));
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
        public static DialogType[] getDilogType() {
            return DialogType.class.getEnumConstants();
        }
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

        /*
            Cette fonction prend un content (un string avec toutes les informations données) et convertie une chaine
            de caractere specifique "!$ valeur : hauteur : largeur!$ en img html
         */
        private String convertIfImagePresent(String content) {
            String src = StringUtils.substringBetween(content, "!$", "!$");
            while (src != null) {
                String src_original = "!$" + src + "!$";
                String[] tab_source = src.split(":");
                if (tab_source.length < 3) {
                    content = content.replace(src_original, "\n ㅤ<img src='moodle/images/" + src + "' >ㅤ");
                } else {
                    if (tab_source[1] == "null") {
                        content = content.replace(src_original, "\n ㅤ<img src='moodle/images/" + tab_source[0] + "' height='" + tab_source[2] + "' >ㅤ");
                    } else if (tab_source[2] == "null") {
                        content = content.replace(src_original, "\n ㅤ<img src='moodle/images/" + tab_source[0] + "' width='" + tab_source[1] + ">ㅤ");
                    } else {
                        content = content.replace(src_original, "\n ㅤ<img src='moodle/images/" + tab_source[0] + "' width='" + tab_source[1] + "' height='" + tab_source[2] + "' >ㅤ");
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
                    .set("width","25px")
                    .set("margin","auto");
            modifyButton.setIcon(img);
            modifyButton.addClickListener(event -> modifyPopup.open());
            return modifyButton;
        }

        /**
         * The created pop-up is invisible until the open() method is called.
         */
        private void createModifyPopup() {
            VerticalLayout layout = new VerticalLayout();
            HorizontalLayout layout_horizontal = new HorizontalLayout();
            HorizontalLayout layout_horizontal_button = new HorizontalLayout();

            AtomicReference<DialogMoodle> dialoglink = new AtomicReference<>(new DialogMoodle(modifyPopup, DialogType.LINK));
            AtomicReference<DialogMoodle> dialogimage = new AtomicReference<>(new DialogMoodle(modifyPopup, DialogType.IMAGE));


            Button link = new Button("Ajouter un lien");
            Button image = new Button("Ajouter une Image");
            Label label = new Label("Modify the section here");
            TextField title = new TextField("Title");
            title.setValue(section.getTitle());
            TextArea content = new TextArea("Content");
            content.setValue(section.getContent());
            Button okButton = new Button("Valider");
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

    /*
        Cette classe permet de générer un dialog avec des Tabs
     */

    public class DialogMoodle extends Dialog {

        private Dialog parent;
        private Map<Tab, Component> tabsToPages = new HashMap<>();
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

        /*
            Cette fonction crée les Tabs
         */
        public void createTab(Div div_interne, Div div_externe, String name) {
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

        /*
            Cette fonction créer la partie du tab qui s'occupe des liens externes
         */
        public Div externeLinkDiv() {
            Div d = new Div();

            HorizontalLayout insertLayout = new HorizontalLayout();
            HorizontalLayout buttonLayout = new HorizontalLayout();
            VerticalLayout mainLayout = new VerticalLayout();

            TextField msg = createTextField("Texte à l'affichage: ", "Entre le nom du lien ici...");
            TextField lien = createTextField("Votre lien: ", "Entre votre lien ici....");
            TextArea text = createTextArea("Texte généré:", "");

            Button valide = new Button("Generer");
            Button copie = new Button("Copier");
            Button close = new Button("Fermer");
            valide.addClickListener(event -> {
                if (msg.isEmpty() || lien.isEmpty()) {
                    text.setValue("Erreur champs invalide");
                } else if (!isLinks(lien.getValue())) {
                    text.setValue("Erreur lien non valide");
                } else {
                    text.setValue("[" + msg.getValue() + "](" + lien.getValue() + ")");
                }
            });

            close.addClickListener(event -> {
                this.close();
                this.parent.open();
            });

            copie.addClickListener(event -> {
                copyInClipBoard(text.getValue());
            });
            buttonLayout.add(valide, copie, close);
            insertLayout.add(msg, lien);
            mainLayout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
            mainLayout.add(insertLayout, text, buttonLayout);
            d.add(mainLayout);
            return d;
        }

        /*
             Cette fonction créer la partie du tab qui s'occupe des liens internes
        */
        public Div interneLinkDiv() {
            Div interne = new Div();
            AtomicReference<String> url = new AtomicReference<>("channels");
            List<Assignment> assignment = getAssignmentController().getAssignmentsForCourse(getCourse().getId());
            List<TextChannel> channels = getController().getAllChannelsForCourse(getCourse().getId());
            List<MoodlePage> moodle = getController().getAllMoodlePageForCourse(getCourse().getId());

            Map<Integer, Component> selectMap = new HashMap<>();


            HorizontalLayout insertLayout = new HorizontalLayout();
            HorizontalLayout buttonLayout = new HorizontalLayout();
            VerticalLayout mainLayout = new VerticalLayout();

            TextField msg = createTextField("Texte à l'affichage: ", "Entre le nom du lien ici...");
            TextArea text = createTextArea("Texte généré:", "");

            Select<Assignment> select_assignment = new Select<>();
            select_assignment.setLabel("Sélectionnez une redirection : ");
            select_assignment.setTextRenderer(Assignment::getName);
            select_assignment.setItems(assignment);

            Select<TextChannel> select_channel = new Select<>();
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
            Button copie = new Button("Copier");
            Button close = new Button("Fermer");
            valide.addClickListener(event -> {
                if (msg.isEmpty()) {
                    text.setValue("Erreur champs invalide");
                } else {
                    //TODO: à changer pour le serveur ne plus mettre https://localhost:8080/
                    String urlRadicale = "http://localhost:8080/";

                    switch (url.get()) {
                        case "channels":
                            if (select_channel.getValue() == null) {
                                text.setValue("Erreur aucune séléction");
                            } else {
                                targetId = select_channel.getValue().getId();
                                text.setValue("[" + msg.getValue() + "](" + urlRadicale + url + "/" + targetId + " )");
                            }
                            break;
                        case "assignment":
                            if (select_assignment.getValue() == null) {
                                text.setValue("Erreur aucune séléction");
                            } else {
                                targetId = select_assignment.getValue().getId();
                                text.setValue("[" + msg.getValue() + "](" + urlRadicale + url + "/" + targetId + " )");
                            }
                            break;
                        case "moodle":
                            if (select_moodle.getValue() == null) {
                                text.setValue("Erreur aucune séléction");
                            } else {
                                targetId = select_moodle.getValue().getId();
                                text.setValue("[" + msg.getValue() + "](" + urlRadicale + url + "/" + targetId + " )");
                            }
                            break;
                    }
                }
            });

            close.addClickListener(event -> {
                this.close();
                this.parent.open();
            });
            copie.addClickListener(event -> {
                copyInClipBoard(text.getValue());
            });

            insertLayout.add(msg, radio, select_assignment, select_channel, select_moodle);
            buttonLayout.add(valide, copie, close);
            mainLayout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
            mainLayout.add(insertLayout, text, buttonLayout);
            interne.add(mainLayout);
            return interne;
        }


        /*
            Cette fonction créer la partie du tab qui s'occupe des images externes
         */
        public Div externeImageDiv() {
            Div d = new Div();

            HorizontalLayout insertLayout = new HorizontalLayout();
            HorizontalLayout buttonLayout = new HorizontalLayout();
            VerticalLayout mainLayout = new VerticalLayout();

            TextField lien = createTextField("Votre lien: ", "Entre votre lien ici....");
            TextArea text = createTextArea("Texte généré:", "");
            Paragraph warning = new Paragraph("Les liens autorisés doivent finir par : .png, .jpg ou .jpeg");

            Button valide = new Button("Generer");
            Button copie = new Button("Copier");
            Button close = new Button("Fermer");
            valide.addClickListener(event -> {
                if (lien.isEmpty()) {
                    text.setValue("Erreur champs invalide");
                } else if (!isLinks(lien.getValue())) {
                    text.setValue("Erreur lien non valide");
                } else if (!isImages(lien.getValue())) {
                    text.setValue("Erreur ce lien n'est pas une image");
                } else {
                    text.setValue("![](" + lien.getValue() + ")");
                }
            });

            close.addClickListener(event -> {
                this.close();
                this.parent.open();
            });

            copie.addClickListener(event -> copyInClipBoard(text.getValue()));
            buttonLayout.add(valide, copie, close);
            insertLayout.add(lien);
            mainLayout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
            mainLayout.add(warning, insertLayout, text, buttonLayout);
            d.add(mainLayout);
            return d;
        }

        /*
             Cette fonction créer la partie du tab qui s'occupe des images internes
        */
        public Div interneImageDiv() {
            Div interne = new Div();

            String newDirName = "src/main/webapp/moodle/images/" + String.valueOf(getCourse().getId());


            UploadComponent uploadComponent = new UploadComponent("100%", "100%", 1, 5242880, newDirName, ".jpg", ".jpeg", ".png");
            uploadComponent.setDropLabel(new Paragraph("Séléctionez votre fichier (5MO max)"));
            String nameChiffre = getRandomId() + "_" + String.valueOf(getCourse().getId());
            HorizontalLayout insertLayout = new HorizontalLayout();
            HorizontalLayout buttonLayout = new HorizontalLayout();
            VerticalLayout mainLayout = new VerticalLayout();

            TextArea text = createTextArea("Texte généré:", "");
            Paragraph warning = new Paragraph("Les fichiers autorisés sont .png, .jpg, .jpeg \n Les limites d'upload sont de 5Mo");

            Div redimension = new Div();
            Paragraph p = new Paragraph("Redimensionnez votre image: (avant Upload) ");
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
            Button close = new Button("Fermer");

            uploadComponent.addSucceededListener(event -> {
                String newPath = "";
                com.vaadin.flow.component.notification.Notification.show("Votre fichier est bien téléchargé");
                try {
                    newPath = renameFile(uploadComponent.getFileName(), newDirName, nameChiffre);
                } catch (Exception e) {
                    text.setValue("Une erreur est survenue, le fichier est inexistant");
                    close();
                    this.parent.open();
                }

                if (ImageExist(newPath)) {
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
                    text.setValue("Une erreur est survenue, le fichier est inexistant");
                }
            });

            uploadComponent.addFileRejectedListener(event -> {
                com.vaadin.flow.component.notification.Notification.show("Enrengistrement de l'image impossible");
                com.vaadin.flow.component.notification.Notification.show(event.getErrorMessage());
            });

            close.addClickListener(event -> {
                this.close();
                this.parent.open();
            });
            copie.addClickListener(event -> copyInClipBoard(text.getValue()));

            insertLayout.add(uploadComponent);
            buttonLayout.add(copie, close);
            mainLayout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
            mainLayout.add(warning, redimension, insertLayout, text, buttonLayout);
            interne.add(mainLayout);
            return interne;
        }

        /*
           Fonction de copy (dependance Maven)
         */
        public void copyInClipBoard(String text) {
            //il faut que ce soit en https
            VaadinClipboard vaadinClipboard = VaadinClipboardImpl.GetInstance();
            vaadinClipboard.copyToClipboard(text, copySuccess -> {
                if (copySuccess) {
                    Notification.show("\'" + text + "\'" + " a bien été copié");
                } else {
                    Notification.show("Erreur la copie n'a pas été effectué", Notification.Type.ERROR_MESSAGE);
                }
            });
        }


        /*
            Cette fonction verifie qu'il s'agit d'un lien et non d'une entree interdite
         */
        public boolean isLinks(String s) {
            if ((s.startsWith("http") || s.startsWith("https")) && s.contains(".")) return true;
            return false;
        }

        /*
             Cette fonction verifie qu'il s'agit d'un lien et non d'une entree interdite
        */
        public boolean isImages(String s) {
            return (s.toLowerCase().endsWith("jpg") || s.toLowerCase().endsWith("png") || s.toLowerCase().endsWith("jpeg")) && s.contains(".");
        }

        /* *** Fonction auxiliaire pour alleger le code  *** */
        public TextField createTextField(String label, String placeHolder) {
            TextField textField = new TextField();
            textField.setLabel(label);
            textField.setPlaceholder(placeHolder);
            return textField;
        }

        public TextArea createTextArea(String label, String placeHolder) {
            TextArea textArea = new TextArea();
            textArea.setLabel(label);
            textArea.setPlaceholder(placeHolder);
            textArea.setReadOnly(true);
            textArea.getStyle().set("margin", "auto").set("width", "100%");
            return textArea;
        }

        public RadioButtonGroup createRadioDefault() {
            RadioButtonGroup radio = new RadioButtonGroup<>();
            radio.setItems("Salon de discussion", "Devoir à rendre", "Moodle présentation");
            radio.setLabel("Sélectionnez le type de redirection: ");
            radio.setValue("Salon de discussion");
            radio.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
            return radio;
        }

        public String getExtensionImage(String name) {
            String[] tab_name = name.toLowerCase().split("\\.");
            if (tab_name.length > 2) return "";
            if (tab_name[1].contains("jpg") || tab_name[1].contains("jpeg") || tab_name[1].contains("png")) {
                return tab_name[1];
            }
            return "";
        }

        public boolean ImageExist(String url) {
            File f = new File(url);
            return f.exists();
        }

        private String renameFile(String oldName, String path, String linkName) throws Exception {
            String extension = getExtensionImage(oldName);
            String newPath = path + "/" + linkName + "." + extension;
            File old = new File(oldName);
            File newFile = new File(newPath); //extension.toLowerCase());
            if (!old.renameTo(newFile)) {
                throw new Exception("File can't be renamed");
            }
            return newPath;
        }

        public long getRandomId() {
            return new Random().nextLong();
        }
        /* *** Fonction auxiliaire pour alleger le code  *** */
    }


}
