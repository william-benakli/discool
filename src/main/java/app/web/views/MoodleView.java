package app.web.views;

import app.controller.AssignmentController;
import app.controller.Controller;
import app.controller.Markdown;
import app.controller.MoodleBroadcaster;
import app.controller.security.SecurityUtils;
import app.jpa_repo.*;
import app.model.courses.Course;
import app.model.courses.CourseSection;
import app.web.layout.Navbar;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

@Route(value = "moodle", layout = Navbar.class)
public class MoodleView extends ViewWithSidebars implements HasDynamicTitle, HasUrlParameter<Long> {

    private final PersonRepository personRepository;
    private final CourseSectionRepository courseSectionRepository;
    private final CourseRepository courseRepository;
    private Course course;

    private final FlexLayout moodleBar = new FlexLayout();

    private Registration broadcasterRegistration;
    private MoodlePage page;

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
        if (c.isPresent()) {
            setCourse(c.get());
        } else {
            throw new Exception("There is no course with this ID.");
            // TODO : take care of the exception
        }
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

            if(! SecurityUtils.isUserStudent()){
                FlexLayout f=new FlexLayout();
                f.add(createDeleteButton(), createModifyButton());
                this.add(f);
            }

            initContent();
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
            Dialog modifyPopup = new Dialog();
            FormLayout popupContent = new FormLayout();
            DialogLink Dialoglink = new DialogLink();
            Button link = new Button("Generer des liens");

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
                Dialoglink.open();
            });

            popupContent.add(label, link, title, content, okButton);
            modifyPopup.add(popupContent);
            modifyPopup.open();
        }

    }


    public class DialogLink extends Dialog{

        Map<Tab, Component> tabsToPages = new HashMap<>();

        DialogLink(){
            Tab externe = new Tab("Lien externe");

            Tab interne = new Tab("Lien interne");


            Tabs tabs = new Tabs(externe, interne);


            tabs.addSelectedChangeListener(event -> {
                tabsToPages.values().forEach(e -> e.setVisible(false));
                tabsToPages.get(tabs.getSelectedTab()).setVisible(true);
            });

        }


        public Div externeLinkDiv(){
            Div d = new Div();
            HorizontalLayout insertLayout = new HorizontalLayout();
            HorizontalLayout buttonLayout = new HorizontalLayout();

            VerticalLayout mainLayout = new VerticalLayout();

            TextField msg = new TextField();
            TextField lien = new TextField();
            insertLayout.add(msg, lien);
            msg.setPlaceholder("Entre le nom du lien ici...");
            lien.setPlaceholder("Entre votre lien ici....");
            TextArea text = new TextArea();
            text.setPlaceholder("Votre text apparaitra ici");

            Button valide = new Button("ddddd");
            Button copie = new Button("Copier");
            Button close = new Button("fermer");
            buttonLayout.add(valide, copie, close);
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
            });
            mainLayout.add(insertLayout, text, buttonLayout);
            d.add(mainLayout);
            return d;
        }


        public boolean isLinks(String s){
            if(s.startsWith("http") && s.contains("www") && s.contains(".")) return true;
            return false;
        }

    }
}
