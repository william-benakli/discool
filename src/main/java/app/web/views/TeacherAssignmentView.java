package app.web.views;

import app.controller.AssignmentController;
import app.controller.Controller;
import app.jpa_repo.*;
import app.model.courses.Assignment;
import app.model.courses.Course;
import app.web.layout.Navbar;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Route(value = "assignment", layout = Navbar.class)
public class TeacherAssignmentView extends ViewWithSidebars implements HasDynamicTitle, HasUrlParameter<Long> {

    private final AssignmentRepository assignmentRepository;
    private final StudentAssignmentsUploadsRepository studentAssignmentsUploadsRepository;
    private final CourseRepository courseRepository;
    private final PersonRepository personRepository;
    private Course course;
    private Assignment assignment;
    private final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();


    private FlexLayout assignmentBar = new FlexLayout();

    public TeacherAssignmentView(@Autowired CourseRepository courseRepository,
                                 @Autowired TextChannelRepository textChannelRepository,
                                 @Autowired AssignmentRepository assignmentRepository,
                                 @Autowired PersonRepository personRepository,
                                 @Autowired StudentAssignmentsUploadsRepository studentAssignmentsUploadsRepository) {
        this.assignmentRepository = assignmentRepository;
        this.studentAssignmentsUploadsRepository = studentAssignmentsUploadsRepository;
        this.courseRepository = courseRepository;
        this.personRepository = personRepository;
        setController(new Controller(personRepository, textChannelRepository, null,
                                     courseRepository, null));
        setAssignmentController(new AssignmentController(personRepository, assignmentRepository,
                                                         studentAssignmentsUploadsRepository, courseRepository));
    }


    @SneakyThrows // so that javac doesn't complain about not catching the exception
    @Override
    public void setParameter(BeforeEvent event, Long parameter) {
        Optional<Assignment> a = assignmentRepository.findById(parameter);
        assignment = a.orElse(null);
        if (assignment == null) {
            throw new Exception("There is no course with this ID.");
            // TODO : take care of the exception (issue 28)
        }
        Optional<Course> c = courseRepository.findById(assignment.getCourseId());
        course = c.orElse(null);
        if (course == null) {
            throw new Exception("There is no course with this ID.");
            // TODO : take care of the exception (issue 28)
        }
        createSidebar(course.getId());
        createMembersBar(course.getId());
        createAdminPanelBar();
        createLayout(assignmentBar);
    }

    @Override
    public String getPageTitle() {
        return course.getName();
    }

    private void createAdminPanelBar() {
        assignmentBar.removeAll();
        setCardStyle(assignmentBar, "60%", ColorHTML.GREY);
        H1 title = new H1(assignment.getName());
        assignmentBar.add(title);
        TeacherLayout layout = new TeacherLayout(assignment);
        assignmentBar.add(layout);
    }

    private class TeacherLayout extends VerticalLayout {
        public TeacherLayout(Assignment assignment) {
            
        }
    }
}
