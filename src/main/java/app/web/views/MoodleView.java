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
import app.model.courses.CourseSection;
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

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Route(value = "moodle", layout = Navbar.class)
public class MoodleView extends ViewWithSidebars implements HasDynamicTitle, HasUrlParameter<Long> {

    private final PersonRepository personRepository;
    private final CourseSectionRepository courseSectionRepository;
    private final CourseRepository courseRepository;
    private Course course;

    private final FlexLayout moodleBar = new FlexLayout();

    private Registration broadcasterRegistration;

    public MoodleView(@Autowired CourseSectionRepository courseSectionRepository,
                      @Autowired CourseRepository courseRepository,
                      @Autowired TextChannelRepository textChannelRepository,
                      @Autowired PersonRepository personRepository,
                      @Autowired AssignmentRepository assignmentRepository,
                      @Autowired StudentAssignmentsUploadsRepository studentAssignmentsUploadsRepository,
                      @Autowired GroupRepository groupRepository,
                      @Autowired GroupMembersRepository groupMembersRepository) {
        this.personRepository=personRepository;
        this.courseSectionRepository = courseSectionRepository;
        this.courseRepository = courseRepository;
        setPersonRepository(personRepository);
        setController(new Controller(personRepository, textChannelRepository, null,
                                     courseRepository, courseSectionRepository, groupRepository, groupMembersRepository));
        setAssignmentController(new AssignmentController(personRepository, assignmentRepository,
                                                         studentAssignmentsUploadsRepository, courseRepository));
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        UI ui = attachEvent.getUI();
        broadcasterRegistration = MoodleBroadcaster.register(newMessage -> ui.access(this::createMoodleBar));
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        broadcasterRegistration.remove();
        broadcasterRegistration = null;
    }

    /**
     * Creates the center panel that contains the Moodle sections
     */
    public void createMoodleBar() {
        moodleBar.removeAll();
        setCardStyle(moodleBar, "60%", ColorHTML.GREY);
        H1 title = new H1(getController().getTitleCourse(course.getId()));
        moodleBar.add(title);
        LinkedList<CourseSection> listOfSections = getController().getAllSectionsInOrder(course.getId());
        for (CourseSection section : listOfSections) {
            SectionLayout sectionLayout = new SectionLayout(section);
            moodleBar.add(sectionLayout);
        }
    }

    @SneakyThrows // so that javac doesn't complain about not catching the exception
    @Override
    public void setParameter(BeforeEvent event, Long parameter) {
        Optional<Course> c = courseRepository.findById(parameter);
        if (c.isPresent()) {
            course = c.get();
        } else {
            throw new Exception("There is no course with this ID.");
            // TODO : take care of the exception
        }
        createSidebar(course.getId());
        createMembersBar(course.getId());
        createMoodleBar();
        createLayout(moodleBar);
    }

    @Override
    public String getPageTitle() {
        return course.getName();
    }

    /**
     * The Layout that contains for each section :
     * - the title
     * - the content
     * - the delete button
     * - the modify button
     */
    public class SectionLayout extends VerticalLayout implements HasText {
        private final CourseSection section;

        // TODO : add icons for the buttons

        private final H2 title = new H2();
        private final Paragraph content = new Paragraph();
        private final Dialog modifyPopup = new Dialog();

        public SectionLayout(CourseSection section) {
            this.section = section;
            if (section == null) return;
            initContent();

            if(! SecurityUtils.isUserStudent()){
                FlexLayout f=new FlexLayout();
                f.add(createDeleteButton(), createModifyButton());
                this.add(f);
            }
            createModifyPopup();
        }

        private void initContent() {
            title.add(Markdown.getHtmlFromMarkdown(section.getTitle()));
            content.add(Markdown.getHtmlFromMarkdown(section.getContent()));
            add(title);
            add(content);
        }

        private Button createDeleteButton() {
            Button deleteButton = new Button();
            Image img = new Image("img/corbeille.svg", "edition");
            img.getStyle()
                    .set("width","25px")
                    .set("margin","auto");
            deleteButton.setIcon(img);
            deleteButton.addClickListener(event -> {
                getController().deleteSection(section);
                MoodleBroadcaster.broadcast("UPDATE_SECTION_DELETED");
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
            //   FormLayout popupContent = new FormLayout();
            VerticalLayout layout = new VerticalLayout();
            HorizontalLayout layout_horizontal = new HorizontalLayout();

            AtomicReference<DialogLink> dialoglink = new AtomicReference<>(new DialogLink(modifyPopup));

            Button link = new Button("Liens");
            Label label = new Label("Modify the section here");
            TextField title = new TextField("Title");
            title.setValue(section.getTitle());
            TextArea content = new TextArea("Content");
            content.setValue(section.getContent());
            Button okButton = new Button("Valider");
            okButton.addClickListener(event -> {
                getController().updateSection(section, title.getValue(), content.getValue());
                modifyPopup.close();
                MoodleBroadcaster.broadcast("UPDATE_SECTION_UPDATED");
            });

            link.addClickListener(event -> {
                dialoglink.set(new DialogLink(modifyPopup));
                dialoglink.get().open();
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


    public class DialogLink extends Dialog {

        Dialog parent;
        Map<Tab, Component> tabsToPages = new HashMap<>();
        long targetId;

        DialogLink(Dialog parent) {
            this.parent = parent;

            Tab externe = new Tab("Lien externe");
            Div div_externe = externeLinkDiv();
            tabsToPages.put(externe, div_externe);

            Tab interne = new Tab("Lien interne");
            Div div_interne = interneLinkDiv();
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


        public Div externeLinkDiv(){
            Div d = new Div();

            HorizontalLayout insertLayout = new HorizontalLayout();
            HorizontalLayout buttonLayout = new HorizontalLayout();
            VerticalLayout mainLayout = new VerticalLayout();

            TextField msg = createTextField("Text à l'affichage: ", "Entre le nom du lien ici...");
            TextField lien = createTextField("Votre lien: ", "Entre votre lien ici....");
            TextArea text = createTextArea("Texte généré:", "Votre text apparaitra ici");

            Button valide = new Button("Generer");
            Button copie = new Button("Copier");
            Button close = new Button("fermer");
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

        public Div interneLinkDiv() {
            Div interne = new Div();
            AtomicReference<String> url = new AtomicReference<>("channels");
            List<Assignment> assigment = getAssignmentController().getAssignmentsForCourse(course.getId());
            List<TextChannel> channels = getController().getAllChannelsForCourse(course.getId());
            Map<Integer, Component> selectMap = new HashMap<>();


            HorizontalLayout insertLayout = new HorizontalLayout();
            HorizontalLayout buttonLayout = new HorizontalLayout();
            VerticalLayout mainLayout = new VerticalLayout();

            TextField msg = createTextField("Text à l'affichage: ", "Entre le nom du lien ici...");
            TextArea text = createTextArea("Texte généré:", "Votre text apparaitra ici");

            Select<Assignment> select_assignment = new Select<>();
            select_assignment.setLabel("Séléctionnez une redirection : ");
            select_assignment.setTextRenderer(Assignment::getName);
            select_assignment.setItems(assigment);

            Select<TextChannel> select_channel = new Select<>();
            select_channel.setLabel("Séléctionnez une redirection : ");
            select_channel.setTextRenderer(TextChannel::getName);
            select_channel.setItems(channels);


            Select<Course> select_moodle = new Select<>();
            select_moodle.setLabel("Séléctionnez une redirection : ");

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
            Button close = new Button("fermer");
            valide.addClickListener(event -> {
                if (msg.isEmpty() || url.get().isEmpty()) {
                    text.setValue("Erreur champs invalide");
                } else {
                    //TODO: à changer pour le serveur ne plus mettre https://localhost:8080/
                    String urlRadicale = "http://localhost:8080/";

                    switch (url.get()) {
                        case "channels":
                            targetId = select_channel.getValue().getId();
                            text.setValue("[" + msg.getValue() + "](" + urlRadicale + url + "/" + targetId + " )");
                            break;
                        case "assignment":
                            targetId = select_assignment.getValue().getId();
                            text.setValue("[" + msg.getValue() + "](" + urlRadicale + url + "/" + targetId + " )");
                            break;
                        case "moodle":
                            targetId = select_moodle.getValue().getId();
                            text.setValue("[" + msg.getValue() + "](" + urlRadicale + url + "/" + targetId + " )");
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

            insertLayout.add(msg, select_assignment, select_channel, radio);
            buttonLayout.add(valide, copie, close);
            mainLayout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
            mainLayout.add(insertLayout, text, buttonLayout);
            interne.add(mainLayout);
            return interne;
        }


        public void copyInClipBoard(String text) {
            //il faut que ce soit en https
            System.out.println(text);
            VaadinClipboard vaadinClipboard = VaadinClipboardImpl.GetInstance();
            vaadinClipboard.copyToClipboard(text, copySuccess -> {
                if (copySuccess) {
                    Notification.show("\'" + text + "\'" + " a bien été copié");
                } else {
                    Notification.show("Erreur la copie n'a pas été effectué", Notification.Type.ERROR_MESSAGE);
                }
            });
        }

        public boolean isLinks(String s) {
            if ((s.startsWith("http") || s.startsWith("https")) && s.contains("www") && s.contains(".")) return true;
            return false;
        }


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
            radio.setLabel("Séléctionnez type de rédirection: ");
            radio.setValue("Salon de discussion");
            return radio;
        }

    }
}
