package app.web.views;

import app.controller.AssignmentController;
import app.controller.Controller;
import app.controller.Markdown;
import app.controller.MoodleBroadcaster;
import app.controller.security.SecurityUtils;
import app.jpa_repo.*;
import app.model.courses.Course;
import app.model.courses.MoodlePage;
import app.web.layout.Navbar;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@Route(value = "moodle", layout = Navbar.class)
public class MoodleView extends ViewWithSidebars implements HasDynamicTitle, HasUrlParameter<Long> {

    private final CourseRepository courseRepository;
    private final MoodlePageRepository moodlePageRepository;

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

        public SectionLayout(MoodlePage section) {
            this.section = section;
            if (section == null) return;

            if(! SecurityUtils.isUserStudent()){
                FlexLayout f=new FlexLayout();
                f.add(createDeleteButton(), createModifyButton());
                this.add(f);
            }

            initContent();
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
            modifyButton.addClickListener(event -> {
                createModifyPopup();
            });
            return modifyButton;
        }

        /**
         * The created pop-up is invisible until the open() method is called.
         */
        private void createModifyPopup() {
            Dialog modifyPopup = new Dialog();
            FormLayout popupContent = new FormLayout();
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

            popupContent.add(label, title, content, okButton);
            modifyPopup.add(popupContent);
            modifyPopup.open();
        }

    }

}
