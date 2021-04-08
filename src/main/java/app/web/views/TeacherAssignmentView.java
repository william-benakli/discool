package app.web.views;

import app.controller.AssignmentController;
import app.controller.Controller;
import app.jpa_repo.*;
import app.model.courses.Assignment;
import app.model.courses.Course;
import app.model.courses.StudentAssignmentUpload;
import app.model.users.Person;
import app.web.layout.Navbar;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Optional;

@Route(value = "teacher_assignment", layout = Navbar.class)
public class TeacherAssignmentView extends ViewWithSidebars implements HasDynamicTitle, HasUrlParameter<Long> {

    private final AssignmentRepository assignmentRepository;
    private final StudentAssignmentsUploadsRepository studentAssignmentsUploadsRepository;
    private final CourseRepository courseRepository;
    private final PersonRepository personRepository;
    private Course course;
    private Assignment assignment;

    private FlexLayout assignmentBar = new FlexLayout();

    public TeacherAssignmentView(@Autowired CourseRepository courseRepository,
                                 @Autowired TextChannelRepository textChannelRepository,
                                 @Autowired AssignmentRepository assignmentRepository,
                                 @Autowired PersonRepository personRepository,
                                 @Autowired StudentAssignmentsUploadsRepository studentAssignmentsUploadsRepository,
                                 @Autowired GroupRepository groupRepository,
                                 @Autowired GroupMembersRepository groupMembersRepository) {
        this.assignmentRepository = assignmentRepository;
        this.studentAssignmentsUploadsRepository = studentAssignmentsUploadsRepository;
        this.courseRepository = courseRepository;
        this.personRepository = personRepository;
        setController(new Controller(personRepository, textChannelRepository, null,
                                     courseRepository, null, groupRepository, groupMembersRepository));
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
        TeacherLayout layout = new TeacherLayout();
        layout.createGrid();
        assignmentBar.add(layout);
    }

    private class TeacherLayout extends VerticalLayout {
        private Grid<RowModel> grid;

        private void createGrid() {
            grid = new Grid<>();
            assignValues();
            createColumns();
            grid.addThemeVariants(GridVariant.LUMO_NO_BORDER,
                                       GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_ROW_STRIPES);
            this.add(grid);
        }

        private void assignValues() {
            ArrayList<Person> students = getController().getAllStudentsForCourse(course.getId());
            ArrayList<RowModel> values = new ArrayList<>();
            students.forEach(student -> {
                StudentAssignmentUpload u = getAssignmentController().
                        findStudentAssignmentSubmission(assignment.getId(), student.getId());
                if (u != null) {
                    values.add(new RowModel(student.getLastName() + " " + student.getFirstName(),
                                            u.getGrade(), u.getTeacherComments(), u.getId()));
                } else {
                    values.add(new RowModel(student.getLastName() + " " + student.getFirstName(), 0, "No submission yet", 0));
                }
            });
            grid.setItems(values);
        }

        private void createColumns() {
            grid.addColumn(RowModel::getName).setHeader("Name");
            grid.addColumn(RowModel::getGrade).setHeader("Grade");
            grid.addColumn(RowModel::getComments).setHeader("Comments").setAutoWidth(true);;
        }

    }

    @Getter
    @Setter
    public class RowModel {
        private String name;
        private int grade;
        private String comments;
        private long id;

        public RowModel(String name, int grade, String comments, long id) {
            this.name = name;
            this.grade = grade;
            this.comments = comments;
            this.id = id;
        }
    }
}
