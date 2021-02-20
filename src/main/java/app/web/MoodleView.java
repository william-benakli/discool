package app.web;

import app.jpa_repo.CourseRepository;
import app.jpa_repo.CourseSectionRepository;
import app.model.courses.Course;
import app.model.courses.CourseSection;
import com.vaadin.flow.component.html.H1;
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;

/**
 * test class to try to display a Moodle page
 */
@Route(value = "courses")
public class MoodleView extends VerticalLayout implements HasDynamicTitle, HasUrlParameter<Long> {
    private final CourseSectionRepository courseSectionRepository;
    private final CourseRepository courseRepository;
    private final VerticalLayout sectionList = new VerticalLayout();
    private Course course;

    public MoodleView(@Autowired CourseSectionRepository courseSectionRepository, @Autowired CourseRepository courseRepository) {
        this.courseSectionRepository = courseSectionRepository;
        this.courseRepository = courseRepository;
        add(new H1("Test d'affichage de la page Moodle"));
        add(sectionList);
        //refresh();
    }

    private void refresh() {
        ArrayList<CourseSection> list = courseSectionRepository.findAllSectionsByCourseId(course.getId()); // get all the sections from the table
        list.forEach(courseSection -> courseSection.addParent(courseSectionRepository)); // add the parent for each element
        LinkedList<CourseSection> sortedList = CourseSection.sort(list); // sort the sections in the right order
        sortedList.stream()
                .map(SectionLayout::new)    // create a new SectionLayout for each
                .forEach(sectionList::add); // add this SectionLayout into the sectionList layout to be displayed
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
        refresh();
    }

    @Override
    public String getPageTitle() {
        return course.getName();
    }

    class SectionLayout extends VerticalLayout {
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
