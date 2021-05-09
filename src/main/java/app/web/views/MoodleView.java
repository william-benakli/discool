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
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import com.vaadin.ui.Notification;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
                      @Autowired TextChannelRepository textChannelRepository,
                      @Autowired PersonRepository personRepository,
                      @Autowired AssignmentRepository assignmentRepository,
                      @Autowired StudentAssignmentsUploadsRepository studentAssignmentsUploadsRepository,
                      @Autowired GroupRepository groupRepository,
                      @Autowired GroupMembersRepository groupMembersRepository,
                      @Autowired DirectMessageRepository directMessageRepository) {
        this.courseRepository = courseRepository;
        this.moodlePageRepository = moodlePageRepository;
        setPersonRepository(personRepository);
        setController(new Controller(personRepository, textChannelRepository, null,
                                     courseRepository, moodlePageRepository, groupRepository, groupMembersRepository, directMessageRepository
                                     ));
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

        private void initContent() {
            title.add(Markdown.getHtmlFromMarkdown(section.getTitle()));
            content.add(Markdown.getHtmlFromMarkdown(section.getContent()));
            add(title);
            add(content);
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

        /**
         * The created pop-up is invisible until the open() method is called.
         */
        private void createModifyPopup() {
            VerticalLayout layout = new VerticalLayout();
            HorizontalLayout layout_horizontal = new HorizontalLayout();

            AtomicReference<DialogLink> dialogLink = new AtomicReference<>(new DialogLink(modifyPopup));

            Button link = new Button("Liens");
            Label label = new Label("Modifiez la page ici : ");
            TextField title = new TextField("Titre");
            title.setValue(section.getTitle());
            TextArea content = new TextArea("Contenu");
            content.setValue(section.getContent());
            Button okButton = new Button("Valider");
            okButton.addClickListener(event -> {
                getController().updateSection(section, title.getValue(), content.getValue());
                modifyPopup.close();
                MoodleBroadcaster.broadcast(this);
            });

            link.addClickListener(event -> {
                dialogLink.set(new DialogLink(modifyPopup));
                dialogLink.get().open();
                modifyPopup.close();
            });
            layout_horizontal.add(okButton, link);
            layout_horizontal.setPadding(true);
            layout_horizontal.setSpacing(true);
            layout.add(label, title, content, layout_horizontal);
            layout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
            modifyPopup.add(layout);
        }

    }

    /**
     * The dialog that opens to let the user generate links
     */
    public class DialogLink extends Dialog {
        private final Dialog parent;
        private final Map<Tab, Component> tabsToPages = new HashMap<>();

        private final Map<Integer, Component> selectMap = new HashMap<>();
        private Select<Assignment> select_assignment;
        private Select<TextChannel> select_channel;
        private Select<MoodlePage> select_moodle;
        private AtomicReference<String> url;

        private TextArea generatedLink;
        private TextField userInputNameLink;
        private TextField userInputLinkUrl;

        DialogLink(Dialog parent) {
            this.parent = parent;
            createTab();
        }

        /**
         * Creates the tabs : one for internal links, one for external ones
         */
        private void createTab() {
            Tab externe = new Tab("Lien externe");
            Div div_externe = externalLinksDiv();
            tabsToPages.put(externe, div_externe);

            Tab interne = new Tab("Lien interne");
            Div div_interne = internalLinksDiv();
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

        /**
         * @return the Div for the user to general external links from
         */
        private Div externalLinksDiv() {
            Div d = new Div();
            HorizontalLayout insertLayout = new HorizontalLayout();
            HorizontalLayout buttonLayout = new HorizontalLayout();
            VerticalLayout mainLayout = new VerticalLayout();

            userInputNameLink = createTextField("Texte à afficher : ", "Entrez le nom du lien ici...");
            userInputLinkUrl = createTextField("Votre lien : ", "Entrez votre lien ici...");
            generatedLink = createTextArea();

            Button valide = new Button("Générer le lien");
            Button copie = new Button("Copier");
            Button close = new Button("Fermer");
            valide.addClickListener(event -> createValidateButtonForExternalLinks());

            close.addClickListener(event -> {
                this.close();
                this.parent.open();
            });

            copie.addClickListener(event -> copyInClipBoard(generatedLink.getValue()));
            buttonLayout.add(valide, copie, close);
            insertLayout.add(userInputNameLink, userInputLinkUrl);
            mainLayout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
            mainLayout.add(insertLayout, generatedLink, buttonLayout);
            d.add(mainLayout);
            return d;
        }

        /**
         * @return the Div for the user to generate internal links from
         */
        private Div internalLinksDiv() {
            Div interne = new Div();
            url = new AtomicReference<>("channels");

            HorizontalLayout insertLayout = new HorizontalLayout();
            HorizontalLayout buttonLayout = new HorizontalLayout();
            VerticalLayout mainLayout = new VerticalLayout();

            userInputNameLink = createTextField("Texte à afficher : ", "Entre le nom du lien ici...");
            generatedLink = createTextArea();

            // the drop-down lists to choose the assignment/moodle page/text channel
            createSelectLists(selectMap);
            // the radio button to trigger the right drop-down list to display
            RadioButtonGroup<String> radio = createRadioDefault();
            radio.addValueChangeListener(event -> changeSelectTab(event.getValue()));

            insertLayout.add(userInputNameLink, radio, select_assignment, select_channel, select_moodle);
            buttonLayout.add(createValidateButton(), createCopyButton(), createCloseButton());
            mainLayout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
            mainLayout.add(insertLayout, generatedLink, buttonLayout);
            interne.add(mainLayout);
            return interne;
        }

        private TextField createTextField(String label, String placeHolder) {
            TextField textField = new TextField();
            textField.setLabel(label);
            textField.setPlaceholder(placeHolder);
            return textField;
        }

        private TextArea createTextArea() {
            TextArea textArea = new TextArea();
            textArea.setLabel("Texte généré:");
            textArea.setPlaceholder("");
            textArea.setReadOnly(true);
            textArea.getStyle().set("margin", "auto").set("width", "100%");
            return textArea;
        }

        private void createValidateButtonForExternalLinks() {
            if (userInputNameLink.isEmpty() || userInputLinkUrl.isEmpty()) {
                generatedLink.setValue("Erreur : champs invalides");
            } else if (!isValidExternalLink(userInputLinkUrl.getValue())) {
                generatedLink.setValue("Erreur : lien non valide");
            } else {
                generatedLink.setValue("[" + userInputNameLink.getValue() + "](" + userInputLinkUrl.getValue() + ")");
            }
        }

        public void copyInClipBoard(String text) {
            VaadinClipboard vaadinClipboard = VaadinClipboardImpl.GetInstance();
            vaadinClipboard.copyToClipboard(text, copySuccess -> {
                if (copySuccess) {
                    Notification.show("'" + text + "' a bien été copié");
                } else {
                    Notification.show("Erreur : la copie n'a pas été effectuée", Notification.Type.ERROR_MESSAGE);
                }
            });
        }

        private void createSelectLists(Map<Integer, Component> selectMap) {
            List<Assignment> assignments = getAssignmentController().getAssignmentsForCourse(getCourse().getId());
            List<TextChannel> channels = getController().getAllChannelsForCourse(getCourse().getId());
            List<MoodlePage> moodlePages = getController().getAllMoodlePageForCourse(getCourse().getId());

            select_assignment = new Select<>();
            select_assignment.setLabel("Sélectionnez une redirection : ");
            select_assignment.setTextRenderer(Assignment::getName);
            select_assignment.setItems(assignments);

            select_channel = new Select<>();
            select_channel.setLabel("Sélectionnez une redirection : ");
            select_channel.setTextRenderer(TextChannel::getName);
            select_channel.setItems(channels);

            select_moodle = new Select<>();
            select_moodle.setLabel("Sélectionnez une redirection : ");
            select_moodle.setTextRenderer(MoodlePage::getTitle);
            select_moodle.setItems(moodlePages);

            selectMap.put(0, select_assignment);
            selectMap.put(1, select_channel);
            selectMap.put(2, select_moodle);
            selectMap.values().forEach(e -> e.setVisible(false));
            select_channel.setVisible(true);
        }

        private RadioButtonGroup<String> createRadioDefault() {
            RadioButtonGroup<String> radio = new RadioButtonGroup<>();
            radio.setItems("Salon de discussion", "Devoir à rendre", "Page moodle");
            radio.setLabel("Sélectionnez le type de redirection: ");
            radio.setValue("Salon de discussion");
            radio.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
            return radio;
        }

        private void changeSelectTab(String pageType) {
            if (pageType.equals("Salon de discussion")) {
                selectMap.values().forEach(e -> e.setVisible(false));
                select_channel.setVisible(true);
                url.set("channels");
            } else if (pageType.equals("Devoir à rendre")) {
                selectMap.values().forEach(e -> e.setVisible(false));
                select_assignment.setVisible(true);
                url.set("assignment");
            } else {
                selectMap.values().forEach(e -> e.setVisible(false));
                select_moodle.setVisible(true);
                url.set("moodle");
            }
        }

        private Button createValidateButton() {
            Button valide = new Button("Générer");
            valide.addClickListener(event -> {
                if (userInputNameLink.isEmpty()) {
                    generatedLink.setValue("Erreur : champs invalides");
                } else {
                    Select selectToChange = switch (url.get()) {
                        case "channels" -> select_channel;
                        case "assignment" -> select_assignment;
                        case "moodle" -> select_moodle;
                        default -> null;
                    };
                    changeSelectValue(selectToChange);

                }
            });
            return valide;
        }

        private Button createCopyButton() {
            Button copy = new Button("Copier");
            copy.addClickListener(event -> copyInClipBoard(generatedLink.getValue()));
            return copy;
        }

        private Button createCloseButton() {
            Button close = new Button("Fermer");
            close.addClickListener(event -> {
                this.close();
                this.parent.open();
            });
            return close;
        }

        private boolean isValidExternalLink(String s) {
            return (s.startsWith("http") || s.startsWith("https")) && s.contains(".");
        }

        private void changeSelectValue(Select select) {
            String urlRadicale = "http://localhost:8080/";
            if (select.getValue() == null) {
                generatedLink.setValue("Erreur aucune séléction");
            } else {
                long targetId;
                if (select.getValue() instanceof Assignment) {
                    targetId = ((Assignment) select.getValue()).getId();
                } else if (select.getValue() instanceof MoodlePage) {
                    targetId = ((MoodlePage) select.getValue()).getId();
                } else {
                    targetId = ((TextChannel) select.getValue()).getId();
                }
                generatedLink.setValue("[" + userInputNameLink.getValue()
                                               + "](" + urlRadicale + url + "/" + targetId + " )");
            }
        }
    }
}
