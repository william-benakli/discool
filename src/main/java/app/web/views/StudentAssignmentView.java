package app.web.views;

import app.controller.Controller;
import app.jpa_repo.CourseRepository;
import app.model.courses.Course;
import app.web.layout.Navbar;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@Route(value = "assignement", layout = Navbar.class)
public class StudentAssignmentView extends ViewWithSidebars implements HasDynamicTitle, HasUrlParameter<Long> {

    private final CourseRepository courseRepository;
    private Course course;

    private FlexLayout assignmentBar;

    public StudentAssignmentView(@Autowired CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
        setController(new Controller(null, null, null,
                                     courseRepository, null));
    }

    private void createAssignmentBar() {

    }

    @SneakyThrows // so that javac doesn't complain about not catching the exception
    @Override
    public void setParameter(BeforeEvent event, Long parameter) {
        Optional<Course> c = courseRepository.findById(parameter);
        if (c.isPresent()) {
            course = c.get();
        } else {
            throw new Exception("There is no course with this ID.");
            // TODO : take care of the exception (issue 28)
        }
        createSidebar(course.getId());
        createMembersBar(course.getId());
        createAssignmentBar();
        createLayout(assignmentBar);
    }

    @Override
    public String getPageTitle() {
        return course.getName();
    }
}
