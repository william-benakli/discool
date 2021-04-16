package app.web.views;

import app.controller.AssignmentController;
import app.controller.Controller;
import app.controller.security.SecurityUtils;
import app.jpa_repo.*;
import app.model.courses.Assignment;
import app.model.courses.Course;
import app.model.courses.StudentAssignmentUpload;
import app.web.components.UploadComponent;
import app.web.layout.Navbar;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
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
public class StudentAssignmentView extends ViewWithSidebars implements HasDynamicTitle, HasUrlParameter<Long> {

    private final AssignmentRepository assignmentRepository;
    private final StudentAssignmentsUploadsRepository studentAssignmentsUploadsRepository;
    private final CourseRepository courseRepository;
    private final PersonRepository personRepository;
    private Course course;
    private Assignment assignment;
    private final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();


    private FlexLayout assignmentBar = new FlexLayout();

    public StudentAssignmentView(@Autowired CourseRepository courseRepository,
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
        setPersonRepository(personRepository);
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
        createAssignmentBar();
        createLayout(assignmentBar);
    }

    @Override
    public String getPageTitle() {
        return course.getName();
    }


    private void createAssignmentBar() {
        assignmentBar.removeAll();
        setCardStyle(assignmentBar, "60%", ColorHTML.GREY);
        H1 title = new H1(assignment.getName());
        assignmentBar.add(title);
        StudentAssignmentLayout layout = new StudentAssignmentLayout(assignment);
        assignmentBar.add(layout);
    }

    private class StudentAssignmentLayout extends VerticalLayout {
        private final Assignment assignment;

        public StudentAssignmentLayout(Assignment assignment) {
            this.assignment = assignment;
            writeInfo();
            createUploadZone();
        }

        private void writeInfo() {
            this.add(new Paragraph(assignment.getDescription()));
            this.add(new Paragraph("due date : " + assignment.getDuedate()));
            if (assignment.getAllowLate() == 1) {
                this.add(new Paragraph("cut off date : " + assignment.getCutoffdate()));
            }
            //this.add(new Paragraph("max number of attempts : " + assignment.getMaxAttempts()));
            this.add(new Paragraph("max grade : " + assignment.getMaxGrade()));

            writeGradeInfo();
        }

        private void writeGradeInfo() {
            StudentAssignmentUpload s = getAssignmentController().findStudentAssignmentSubmission(assignment.getId(),
                                                 personRepository.findByUsername(authentication.getName()).getId());
            Paragraph grade = new Paragraph();
            this.add(grade);
            if (s != null) {
                if (s.getGrade() == -1) {
                    grade.setText("Your assignment hasn't been graded yet");
                } else {
                    grade.setText("Your grade is : " + s.getGrade());
                    if (s.getTeacherComments() == null) {
                        this.add(new Paragraph("Your teacher didn't write any comments"));
                    } else {
                        this.add(new Paragraph("Teacher's comments : \n" + s.getTeacherComments()));
                    }
                }
            } else {
                this.add(new Paragraph("You have not submitted an answer yet !"));
            }
        }

        private void createUploadZone() {
            String newDirName = "uploads/assignments/" + String.valueOf(assignment.getId()) + "_" +
                    String.valueOf(SecurityUtils.getCurrentUser(personRepository).getId());
            UploadComponent upload = new UploadComponent("200px", "200px", 3, 30000000,
                                                         newDirName);

            upload.addSucceededListener(event -> {
                getAssignmentController().saveStudentUploadIfNeeded(assignment.getId(), assignment.getCourseId(),
                                                                    personRepository.findByUsername(authentication.getName()).getId());
                Notification.show("You successfully uploaded your file !");
            });

            upload.addFileRejectedListener(event -> {
                Notification.show("Couldn't upload the file");
                Notification.show(event.getErrorMessage());
            });

            this.add(upload);
        }
    }
}
