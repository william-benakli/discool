package app.web.views;

import app.controller.Controller;
import app.jpa_repo.CourseRepository;
import app.jpa_repo.CourseSectionRepository;
import app.jpa_repo.TextChannelRepository;
import app.jpa_repo.UserRepository;
import app.model.courses.Course;
import app.model.courses.CourseSection;
import app.web.layout.CourseLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
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
@Route(value = "courses", layout = CourseLayout.class)
public class MoodleView extends ViewWithSidebars implements HasDynamicTitle, HasUrlParameter<Long> {
    private final CourseSectionRepository courseSectionRepository;
    private final CourseRepository courseRepository;
    private Course course;

    private FlexLayout moodleBar = new FlexLayout();


    public MoodleView(@Autowired CourseSectionRepository courseSectionRepository,
                      @Autowired CourseRepository courseRepository,
                      @Autowired TextChannelRepository textChannelRepository,
                      @Autowired UserRepository userRepository) {
        this.courseSectionRepository = courseSectionRepository;
        this.courseRepository = courseRepository;
        setController(new Controller(userRepository, textChannelRepository, null,
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


    public class SectionLayout extends VerticalLayout {
        TextField title = new TextField(); // will be filled with the value of the title field in the CourseSection
        TextArea content = new TextArea(); // same with the content field

        public SectionLayout(CourseSection section) {
            add(title, content);

            // The UI fields on *this* will be bound to the fields of the same name in the CourseSection class,
            // taking the value from the *section* object
            Binder<CourseSection> binder = new Binder<>(CourseSection.class);
            binder.bindInstanceFields(this);
            binder.setBean(section);

            // for when the values of the fields change
            binder.addValueChangeListener(e -> {
                courseSectionRepository.save(binder.getBean()); // save the new values
                refresh();
            });
        }
    }
}
