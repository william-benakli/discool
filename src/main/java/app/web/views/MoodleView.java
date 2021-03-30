package app.web.views;

import app.controller.Controller;
import app.controller.Markdown;
import app.controller.MoodleBroadcaster;
import app.jpa_repo.CourseRepository;
import app.jpa_repo.CourseSectionRepository;
import app.jpa_repo.PersonRepository;
import app.jpa_repo.TextChannelRepository;
import app.model.courses.Course;
import app.model.courses.CourseSection;
import app.web.components.ComponentButton;
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

import java.util.LinkedList;
import java.util.Optional;

@Route(value = "moodle", layout = Navbar.class)
public class MoodleView extends ViewWithSidebars implements HasDynamicTitle, HasUrlParameter<Long> {

    private final CourseSectionRepository courseSectionRepository;
    private final CourseRepository courseRepository;
    private Course course;

    private final FlexLayout moodleBar = new FlexLayout();

    private Registration broadcasterRegistration;

    public MoodleView(@Autowired CourseSectionRepository courseSectionRepository,
                      @Autowired CourseRepository courseRepository,
                      @Autowired TextChannelRepository textChannelRepository,
                      @Autowired PersonRepository personRepository) {
        this.courseSectionRepository = courseSectionRepository;
        this.courseRepository = courseRepository;
        setController(new Controller(personRepository, textChannelRepository, null,
                                     courseRepository, courseSectionRepository));
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
            FlexLayout f=new FlexLayout();
            f.add(createDeleteButton(), createModifyButton());
            this.add(f);
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
                MoodleBroadcaster.broadcast("UPDATE_SECTION_UPDATED");
            });

            popupContent.add(label, title, content, okButton);
            modifyPopup.add(popupContent);
        }

    }

}
