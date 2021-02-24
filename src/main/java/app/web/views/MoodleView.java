package app.web.views;

import app.controller.Controller;
import app.controller.Markdown;
import app.jpa_repo.CourseRepository;
import app.jpa_repo.CourseSectionRepository;
import app.jpa_repo.PersonRepository;
import app.jpa_repo.TextChannelRepository;
import app.model.courses.Course;
import app.model.courses.CourseSection;
import app.web.layout.Navbar;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedList;
import java.util.Optional;

/**
 * test class to try to display a Moodle page
 */
@Route(value = "moodle", layout = Navbar.class)
public class MoodleView extends ViewWithSidebars implements HasDynamicTitle, HasUrlParameter<Long> {
    private final CourseSectionRepository courseSectionRepository;
    private final CourseRepository courseRepository;
    private Course course;

    private final FlexLayout moodleBar = new FlexLayout();


    public MoodleView(@Autowired CourseSectionRepository courseSectionRepository,
                      @Autowired CourseRepository courseRepository,
                      @Autowired TextChannelRepository textChannelRepository,
                      @Autowired PersonRepository personRepository) {
        this.courseSectionRepository = courseSectionRepository;
        this.courseRepository = courseRepository;
        setController(new Controller(personRepository, textChannelRepository, null,
                                     courseRepository, courseSectionRepository));
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

    @Override
    public String getPageTitle() {
        return course.getName();
    }

    private void refresh() {
        createMoodleBar();
    }


    /**
     * The Layout that contains for each section :
     * - the title
     * - the content
     * - the delete button
     * - the modify button
     */
    public static class SectionLayout extends VerticalLayout implements HasText {
        public SectionLayout(CourseSection section) {
            H2 title = new H2();
            Paragraph content = new Paragraph();
            title.add(Markdown.getHtmlFromMarkdown(section.getTitle()));
            content.add(Markdown.getHtmlFromMarkdown(section.getContent()));
            add(title);
            add(content);
        }

    }

}
